package edu.berkeley.nlp.lm.cache;

import java.util.Arrays;

/**
 * A direct-mapped cache. This cache does not perform any collision resolution,
 * but rather retains only the most recent key which gets hashed to a particular
 * bucket.
 * 
 * @author adampauls
 * 
 */
public final class ArrayEncodedDirectMappedLmCache implements ArrayEncodedLmCache
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int VAL_OFFSET = 0;

	private static final int LENGTH_OFFSET = 1;

	private static final int KEY_OFFSET = 2;

	// for efficiency, this array fakes a struct with fields:
	// float val;
	// int length;
	// int[maxNgramOrder] key; 
	private final int[] array;

	private final int cacheSize;

	private final int structLength;

	public ArrayEncodedDirectMappedLmCache(final int cacheBits, final int maxNgramOrder) {
		cacheSize = (1 << cacheBits) - 1;
		this.structLength = (maxNgramOrder + 2);
		array = new int[cacheSize * structLength];
		Arrays.fill(array, Float.floatToIntBits(Float.NaN));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.berkeley.nlp.mt.lm.cache.LmCache#getCached(int[], int, int, int)
	 */
	@Override
	public float getCached(final int[] ngram, final int startPos, final int endPos, final int hash) {
		final float f = getVal(hash);
		if (!Float.isNaN(f)) {
			final int cachedNgramLength = getLength(hash);
			if (equals(ngram, startPos, endPos, array, getKeyStart(hash), cachedNgramLength)) { return f; }
		}
		return Float.NaN;
	}
	
	private boolean equals(final int[] ngram, final int startPos, final int endPos, final int[] cachedNgram, int cachedNgramStart, int cachedNgramLength) {
		if (cachedNgramLength != endPos - startPos) return false;
		for (int i = startPos; i < endPos; ++i) {
			if (cachedNgram[cachedNgramStart + i - startPos] != ngram[i]) return false;
		}
		return true;
	}

	private float getVal(int hash) {
		return Float.intBitsToFloat(array[startOfStruct(hash) + VAL_OFFSET]);
	}

	private float setVal(int hash, float f) {
		return array[startOfStruct(hash) + VAL_OFFSET] = Float.floatToIntBits(f);
	}

	private float setLength(int hash, int l) {
		return array[startOfStruct(hash) + LENGTH_OFFSET] = l;
	}

	private int getLength(int hash) {
		return array[startOfStruct(hash) + LENGTH_OFFSET];
	}

	private int getKeyStart(int hash) {
		return startOfStruct(hash) + KEY_OFFSET;
	}

	/**
	 * @param hash
	 * @return
	 */
	private int startOfStruct(final int hash) {
		return hash * structLength;
	}

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.berkeley.nlp.mt.lm.cache.LmCache#clear()
	 */
	@Override
	public void clear() {
		Arrays.fill(array, Float.floatToIntBits(Float.NaN));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.berkeley.nlp.mt.lm.cache.LmCache#putCached(int[], int, int,
	 * float, int)
	 */
	@Override
	public void putCached(final int[] ngram, final int startPos, final int endPos, final float f, final int hash) {
		setLength(hash, endPos - startPos);
		System.arraycopy(ngram, startPos, array, getKeyStart(hash), endPos - startPos);
		setVal(hash, f);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.berkeley.nlp.mt.lm.cache.LmCache#size()
	 */
	@Override
	public int capacity() {
		return cacheSize;
	}
}