/*
 * Copyright (C) 2013 by SUTD (Singapore)
 * All rights reserved.
 *
 * 	Author: SUTD
 *  Version:  $Revision: 1 $
 */

package cfg.utils;

/**
 * @author LLT
 *
 */
public interface Predicate<T> {
	public boolean apply(T val);
}
