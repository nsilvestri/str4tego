package model;

import java.io.Serializable;

public class Pair<L, R> implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final L left;
	private final R right;

	public Pair(L left, R right)
	{
		this.left = left;
		this.right = right;
	}

	public L getLeft()
	{
		return left;
	}

	public R getRight()
	{
		return right;
	}

	@Override
	public boolean equals(Object o)
	{
		if (!(o instanceof Pair))
			return false;
		@SuppressWarnings("unchecked")
		Pair<L, R> pairo = (Pair<L, R>) o;
		return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
	}

}