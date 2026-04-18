#!/usr/bin/env python3
"""
Lorapok Keuboard — Bengali AI Model Training Pipeline
======================================================
Trains a Bidirectional LSTM model for Bengali next-word prediction.

Architecture:
  Input:  128-dimensional word embeddings
  Hidden: 2× Bidirectional LSTM (256 units each)
  Dropout: 0.3
  Dense:  512 units (ReLU)
  Output: Softmax over 50,000 vocabulary

Usage:
  python train_bengali_model.py --data ./corpus/merged_corpus.txt --epochs 20
  python train_bengali_model.py --convert --model ./models/bengali_lstm.h5
  python train_bengali_model.py --evaluate --model ./models/bengali_lstm.h5

Requirements:
  pip install tensorflow numpy tqdm
"""

import os
import sys
import json
import pickle
import logging
import argparse
import numpy as np
from pathlib import Path
from typing import List, Dict, Tuple, Optional
from collections import Counter

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)
logger = logging.getLogger("LorapokTrainer")

# ============================================================================
# Configuration
# ============================================================================

class ModelConfig:
    """Model hyperparameters and configuration."""

    # Vocabulary
    VOCAB_SIZE = 50000
    UNK_TOKEN = "<UNK>"
    PAD_TOKEN = "<PAD>"
    BOS_TOKEN = "<BOS>"
    EOS_TOKEN = "<EOS>"

    # Embedding
    EMBEDDING_DIM = 128

    # LSTM
    LSTM_UNITS = 256
    NUM_LSTM_LAYERS = 2
    BIDIRECTIONAL = True
    DROPOUT = 0.3
    RECURRENT_DROPOUT = 0.2

    # Dense
    DENSE_UNITS = 512

    # Training
    BATCH_SIZE = 64
    EPOCHS = 20
    LEARNING_RATE = 0.001
    SEQUENCE_LENGTH = 20
    VALIDATION_SPLIT = 0.1
    EARLY_STOPPING_PATIENCE = 3

    # Output
    MODEL_DIR = "./models"
    MODEL_NAME = "bengali_lstm"

    def to_dict(self) -> Dict:
        return {k: v for k, v in vars(type(self)).items() if not k.startswith('_')}


# ============================================================================
# Data Preprocessing
# ============================================================================

class TextPreprocessor:
    """Tokenize, build vocabulary, and create training sequences."""

    def __init__(self, config: ModelConfig):
        self.config = config
        self.word2idx = {}
        self.idx2word = {}
        self.vocab_size = 0

    def build_vocabulary(self, corpus_file: str) -> Dict[str, int]:
        """Build vocabulary from corpus file."""
        logger.info(f"Building vocabulary from {corpus_file}...")
        word_counts = Counter()

        with open(corpus_file, 'r', encoding='utf-8') as f:
            for line in f:
                tokens = line.strip().split()
                word_counts.update(tokens)

        # Special tokens
        self.word2idx = {
            self.config.PAD_TOKEN: 0,
            self.config.UNK_TOKEN: 1,
            self.config.BOS_TOKEN: 2,
            self.config.EOS_TOKEN: 3,
        }

        # Add most common words
        most_common = word_counts.most_common(self.config.VOCAB_SIZE - 4)
        for idx, (word, count) in enumerate(most_common, start=4):
            self.word2idx[word] = idx

        self.idx2word = {v: k for k, v in self.word2idx.items()}
        self.vocab_size = len(self.word2idx)

        logger.info(f"Vocabulary built: {self.vocab_size} words")
        logger.info(f"Most common: {most_common[:10]}")
        logger.info(f"Total unique words in corpus: {len(word_counts)}")
        return self.word2idx

    def save_vocabulary(self, output_dir: str):
        """Save vocabulary to disk."""
        Path(output_dir).mkdir(parents=True, exist_ok=True)
        vocab_path = os.path.join(output_dir, "vocabulary.json")
        with open(vocab_path, 'w', encoding='utf-8') as f:
            json.dump(self.word2idx, f, ensure_ascii=False, indent=2)
        logger.info(f"Vocabulary saved to {vocab_path}")

    def load_vocabulary(self, vocab_path: str):
        """Load vocabulary from disk."""
        with open(vocab_path, 'r', encoding='utf-8') as f:
            self.word2idx = json.load(f)
        self.idx2word = {v: k for k, v in self.word2idx.items()}
        self.vocab_size = len(self.word2idx)
        logger.info(f"Vocabulary loaded: {self.vocab_size} words")

    def text_to_sequences(self, text: str) -> List[int]:
        """Convert text to sequence of indices."""
        tokens = text.strip().split()
        return [self.word2idx.get(t, self.word2idx[self.config.UNK_TOKEN]) for t in tokens]

    def create_training_data(self, corpus_file: str) -> Tuple[np.ndarray, np.ndarray]:
        """Create input-output pairs for training."""
        logger.info("Creating training sequences...")
        sequences = []

        with open(corpus_file, 'r', encoding='utf-8') as f:
            for line in f:
                seq = self.text_to_sequences(line)
                if len(seq) < 3:
                    continue
                # Add BOS/EOS
                seq = [self.word2idx[self.config.BOS_TOKEN]] + seq + [self.word2idx[self.config.EOS_TOKEN]]
                # Create sliding windows
                for i in range(1, len(seq)):
                    start = max(0, i - self.config.SEQUENCE_LENGTH)
                    sequences.append(seq[start:i + 1])

        # Pad sequences
        max_len = self.config.SEQUENCE_LENGTH + 1
        padded = []
        for seq in sequences:
            if len(seq) > max_len:
                seq = seq[-max_len:]
            padded.append(
                [self.word2idx[self.config.PAD_TOKEN]] * (max_len - len(seq)) + seq
            )

        data = np.array(padded)
        X = data[:, :-1]  # Input: all tokens except last
        y = data[:, -1]   # Target: last token

        logger.info(f"Training data created: X={X.shape}, y={y.shape}")
        logger.info(f"Total sequences: {len(sequences)}")
        return X, y


# ============================================================================
# Model Architecture
# ============================================================================

def build_model(config: ModelConfig) -> 'tf.keras.Model':
    """Build Bidirectional LSTM model for next-word prediction."""
    import tensorflow as tf
    from tensorflow.keras import layers, Model

    logger.info("Building model architecture...")

    # Input
    inputs = layers.Input(shape=(config.SEQUENCE_LENGTH,), name="input_sequence")

    # Embedding
    x = layers.Embedding(
        input_dim=config.VOCAB_SIZE,
        output_dim=config.EMBEDDING_DIM,
        mask_zero=True,
        name="word_embedding"
    )(inputs)

    # LSTM layers
    for i in range(config.NUM_LSTM_LAYERS):
        return_sequences = (i < config.NUM_LSTM_LAYERS - 1)
        lstm = layers.LSTM(
            config.LSTM_UNITS,
            return_sequences=return_sequences,
            dropout=config.DROPOUT,
            recurrent_dropout=config.RECURRENT_DROPOUT,
            name=f"lstm_{i+1}"
        )
        if config.BIDIRECTIONAL:
            x = layers.Bidirectional(lstm, name=f"bi_lstm_{i+1}")(x)
        else:
            x = lstm(x)
        x = layers.Dropout(config.DROPOUT, name=f"dropout_{i+1}")(x)

    # Dense
    x = layers.Dense(config.DENSE_UNITS, activation='relu', name="dense_1")(x)
    x = layers.Dropout(config.DROPOUT, name="dropout_dense")(x)

    # Output
    outputs = layers.Dense(config.VOCAB_SIZE, activation='softmax', name="output")(x)

    model = Model(inputs=inputs, outputs=outputs, name="LorapokPredictor")

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=config.LEARNING_RATE),
        loss='sparse_categorical_crossentropy',
        metrics=['accuracy']
    )

    model.summary(print_fn=logger.info)
    return model


# ============================================================================
# Training
# ============================================================================

def train_model(model, X_train, y_train, config: ModelConfig, output_dir: str):
    """Train the model with callbacks."""
    import tensorflow as tf

    Path(output_dir).mkdir(parents=True, exist_ok=True)
    model_path = os.path.join(output_dir, f"{config.MODEL_NAME}.h5")
    best_model_path = os.path.join(output_dir, f"{config.MODEL_NAME}_best.h5")

    callbacks = [
        tf.keras.callbacks.EarlyStopping(
            monitor='val_loss',
            patience=config.EARLY_STOPPING_PATIENCE,
            restore_best_weights=True,
            verbose=1
        ),
        tf.keras.callbacks.ModelCheckpoint(
            best_model_path,
            monitor='val_loss',
            save_best_only=True,
            verbose=1
        ),
        tf.keras.callbacks.ReduceLROnPlateau(
            monitor='val_loss',
            factor=0.5,
            patience=2,
            min_lr=1e-6,
            verbose=1
        ),
        tf.keras.callbacks.TensorBoard(
            log_dir=os.path.join(output_dir, "logs"),
            histogram_freq=1
        )
    ]

    logger.info(f"Starting training: {config.EPOCHS} epochs, batch_size={config.BATCH_SIZE}")

    history = model.fit(
        X_train, y_train,
        batch_size=config.BATCH_SIZE,
        epochs=config.EPOCHS,
        validation_split=config.VALIDATION_SPLIT,
        callbacks=callbacks,
        verbose=1
    )

    # Save final model
    model.save(model_path)
    logger.info(f"Model saved to {model_path}")

    # Save training history
    history_path = os.path.join(output_dir, "training_history.json")
    history_data = {k: [float(v) for v in vals] for k, vals in history.history.items()}
    with open(history_path, 'w') as f:
        json.dump(history_data, f, indent=2)

    return history


# ============================================================================
# Evaluation
# ============================================================================

def evaluate_model(model, X_test, y_test, idx2word: Dict, config: ModelConfig) -> Dict:
    """Evaluate model with Top-1, Top-3, Top-5 accuracy."""
    logger.info("Evaluating model...")

    predictions = model.predict(X_test, batch_size=config.BATCH_SIZE)
    total = len(y_test)

    top1_correct = 0
    top3_correct = 0
    top5_correct = 0

    for i in range(total):
        true_idx = y_test[i]
        pred_probs = predictions[i]
        top5_indices = pred_probs.argsort()[-5:][::-1]

        if true_idx == top5_indices[0]:
            top1_correct += 1
        if true_idx in top5_indices[:3]:
            top3_correct += 1
        if true_idx in top5_indices:
            top5_correct += 1

    results = {
        "total_samples": total,
        "top1_accuracy": round(top1_correct / total * 100, 2),
        "top3_accuracy": round(top3_correct / total * 100, 2),
        "top5_accuracy": round(top5_correct / total * 100, 2),
    }

    logger.info(f"Top-1 Accuracy: {results['top1_accuracy']}%")
    logger.info(f"Top-3 Accuracy: {results['top3_accuracy']}%")
    logger.info(f"Top-5 Accuracy: {results['top5_accuracy']}%")
    logger.info(f"Targets: Top-1 >38%, Top-3 >67%, Top-5 >81%")

    return results


# ============================================================================
# TensorFlow Lite Conversion
# ============================================================================

def convert_to_tflite(model_path: str, output_dir: str, quantize: bool = True) -> str:
    """Convert Keras model to TensorFlow Lite for mobile deployment."""
    import tensorflow as tf

    logger.info(f"Converting model to TFLite: {model_path}")
    model = tf.keras.models.load_model(model_path)

    converter = tf.lite.TFLiteConverter.from_keras_model(model)

    if quantize:
        logger.info("Applying INT8 quantization (FP32 → INT8)...")
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        converter.target_spec.supported_types = [tf.int8]

    tflite_model = converter.convert()

    tflite_filename = "bengali_lstm_quantized.tflite" if quantize else "bengali_lstm.tflite"
    tflite_path = os.path.join(output_dir, tflite_filename)

    with open(tflite_path, 'wb') as f:
        f.write(tflite_model)

    original_size = os.path.getsize(model_path) / (1024 * 1024)
    tflite_size = os.path.getsize(tflite_path) / (1024 * 1024)
    reduction = round((1 - tflite_size / original_size) * 100, 1) if original_size > 0 else 0

    logger.info(f"TFLite model saved: {tflite_path}")
    logger.info(f"Original: {original_size:.1f} MB → TFLite: {tflite_size:.1f} MB ({reduction}% reduction)")

    return tflite_path


# ============================================================================
# Prediction (Inference)
# ============================================================================

class BengaliPredictor:
    """Load trained model and make next-word predictions."""

    def __init__(self, model_path: str, vocab_path: str, config: ModelConfig):
        import tensorflow as tf
        self.config = config
        self.model = tf.keras.models.load_model(model_path)

        with open(vocab_path, 'r', encoding='utf-8') as f:
            self.word2idx = json.load(f)
        self.idx2word = {int(v): k for k, v in self.word2idx.items()}

    def predict_next(self, text: str, top_k: int = 5) -> List[Tuple[str, float]]:
        """Predict next word given input text."""
        tokens = text.strip().split()
        seq = [self.word2idx.get(t, self.word2idx.get(self.config.UNK_TOKEN, 1)) for t in tokens]

        # Pad/truncate
        if len(seq) > self.config.SEQUENCE_LENGTH:
            seq = seq[-self.config.SEQUENCE_LENGTH:]
        else:
            seq = [0] * (self.config.SEQUENCE_LENGTH - len(seq)) + seq

        input_array = np.array([seq])
        predictions = self.model.predict(input_array, verbose=0)[0]

        top_indices = predictions.argsort()[-top_k:][::-1]
        results = []
        for idx in top_indices:
            word = self.idx2word.get(idx, self.config.UNK_TOKEN)
            prob = float(predictions[idx])
            if word not in (self.config.PAD_TOKEN, self.config.UNK_TOKEN):
                results.append((word, round(prob, 4)))

        return results


# ============================================================================
# Main CLI
# ============================================================================

def main():
    parser = argparse.ArgumentParser(
        description="Lorapok Keuboard — Bengali AI Model Training",
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--data", type=str, default="./corpus/merged_corpus.txt",
                        help="Path to training corpus")
    parser.add_argument("--epochs", type=int, default=20,
                        help="Number of training epochs")
    parser.add_argument("--batch-size", type=int, default=64,
                        help="Training batch size")
    parser.add_argument("--vocab-size", type=int, default=50000,
                        help="Maximum vocabulary size")
    parser.add_argument("--output", type=str, default="./models",
                        help="Output directory for model and vocabulary")
    parser.add_argument("--convert", action="store_true",
                        help="Convert trained model to TFLite")
    parser.add_argument("--model", type=str, default=None,
                        help="Path to trained model (for convert/evaluate)")
    parser.add_argument("--evaluate", action="store_true",
                        help="Evaluate trained model")
    parser.add_argument("--predict", type=str, default=None,
                        help="Predict next word for given text")
    parser.add_argument("--no-quantize", action="store_true",
                        help="Skip INT8 quantization during conversion")

    args = parser.parse_args()
    config = ModelConfig()
    config.EPOCHS = args.epochs
    config.BATCH_SIZE = args.batch_size
    config.VOCAB_SIZE = args.vocab_size

    if args.convert:
        model_path = args.model or os.path.join(args.output, f"{config.MODEL_NAME}.h5")
        convert_to_tflite(model_path, args.output, quantize=not args.no_quantize)
        return

    if args.predict:
        model_path = args.model or os.path.join(args.output, f"{config.MODEL_NAME}.h5")
        vocab_path = os.path.join(args.output, "vocabulary.json")
        predictor = BengaliPredictor(model_path, vocab_path, config)
        results = predictor.predict_next(args.predict)
        print(f"\nInput: {args.predict}")
        print("Predictions:")
        for word, prob in results:
            print(f"  {word}: {prob:.4f}")
        return

    # ---- Full training pipeline ----
    logger.info("=== Lorapok Keuboard AI Training Pipeline ===")

    # 1. Preprocess
    preprocessor = TextPreprocessor(config)
    preprocessor.build_vocabulary(args.data)
    preprocessor.save_vocabulary(args.output)

    # 2. Create training data
    X, y = preprocessor.create_training_data(args.data)

    # 3. Split train/test
    split_idx = int(len(X) * 0.9)
    X_train, X_test = X[:split_idx], X[split_idx:]
    y_train, y_test = y[:split_idx], y[split_idx:]
    logger.info(f"Train: {X_train.shape}, Test: {X_test.shape}")

    # 4. Build model
    import tensorflow as tf
    model = build_model(config)

    # 5. Train
    train_model(model, X_train, y_train, config, args.output)

    # 6. Evaluate
    results = evaluate_model(model, X_test, y_test, preprocessor.idx2word, config)

    # 7. Convert to TFLite
    model_path = os.path.join(args.output, f"{config.MODEL_NAME}.h5")
    convert_to_tflite(model_path, args.output, quantize=True)

    # 8. Save config
    config_path = os.path.join(args.output, "model_config.json")
    with open(config_path, 'w') as f:
        json.dump({**config.to_dict(), **results}, f, indent=2)

    logger.info("=== Training pipeline complete ===")
    logger.info(f"Model: {args.output}/{config.MODEL_NAME}.h5")
    logger.info(f"TFLite: {args.output}/bengali_lstm_quantized.tflite")
    logger.info(f"Vocab: {args.output}/vocabulary.json")


if __name__ == "__main__":
    main()
