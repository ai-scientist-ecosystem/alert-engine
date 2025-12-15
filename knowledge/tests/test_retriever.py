"""Tests for retriever module."""

from knowledge.retriever import Retriever


def test_retrieve() -> None:
    """Test retriever retrieve method."""
    r = Retriever()
    results = r.retrieve("test")
    assert results and "test" in results[0]
