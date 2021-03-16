package ca.yorku.cse.mack.softkeyboard;
import java.io.*;
import java.util.*;

/**
 * This class implements the Minimum String Distance (MSD) algorithm.  An
 * interface is provided to separate the input data from the implementation,
 * and so this implementation can be applied not just to strings of characters
 * but also to words (and it should be relatively easy to support other data
 * types as well.<p>
 *
 * This class has a {@link #main} function that provides command-line
 * functionality for experimenting with the MSD algorithm.<p>
 *
 * This software is Copyright (C) 2004, by William Soukoreff and
 * Scott MacKenzie.
 *
 * @author William Soukoreff & Scott MacKenzie, 2004
 */
public class MSD2
{
	private static String newline = System.getProperty("line.separator");


	static final int     MAX_ALIGNMENT_COUNT   = 200;
	static final boolean OUTPUT_PURGE_MESSAGES = true;


	//-------------------------------------------------------------------------
	// The input data for the MSD algorithm is provided through the following
	// interface (MSDCollection).  This allows the MSD algorithm to be applied
	// to a range of data types.  (But we're only interested in the MSD of
	// strings of characters, and vectors of words.  Implementations of the
	// MSDCollection interface follow.)
	//-------------------------------------------------------------------------

	/**
	 * The parameters to the MSD algorithm are provided through this interface.
	 * Basically the MSD needs access to the presented and transcribed texts,
	 * and also needs a means to compare elements of these texts.
	 */
	public static interface MSDCollection
	{
		public int    getPresentedSize();
		public String getPresentedItem(int index);
		public int    getTranscribedSize();
		public String getTranscribedItem(int index);

		public int    getItemDistance(int p, int t);

		public static final int Infinity = Integer.MAX_VALUE >> 2;
		public int    getTransposedDistance(int p, int t);
	}


	//----------------------------------------------------------
	// Here follow some useful implementations of MSDCollection
	//----------------------------------------------------------

	/**
	 * An MSDCollection where the presented and transcribed text strings are
	 * simple Java Strings, and the MSD compares the individual characters
	 * within the strings.
	 */
	public static class TwoStringCollection implements MSDCollection
	{
		String P;
		String T;
		boolean AllowTransposes = false;

		public TwoStringCollection(String PresentedText, String TranscribedText)
		{
			P = PresentedText;
			T = TranscribedText;
		}

		public TwoStringCollection(String PresentedText,
				String TranscribedText,
				boolean transposes)
		{
			P = PresentedText;
			T = TranscribedText;
			AllowTransposes = transposes;
		}

		//----------------------------------
		// implement the required interface
		//----------------------------------

		public int getPresentedSize()
		{
			return P.length();
		}

		public String getPresentedItem(int index)
		{
			return "" + P.charAt(index);
		}

		public int getTranscribedSize()
		{
			return T.length();
		}

		public String getTranscribedItem(int index)
		{
			return "" + T.charAt(index);
		}

		public int getItemDistance(int p, int t)
		{
			char Pchar = P.charAt(p);
			char Tchar = T.charAt(t);

			if(Pchar == Tchar)
				return 0;
			else
				return 1;
		}

		public int getTransposedDistance(int p, int t)
		{
			if(!AllowTransposes || p < 1 || t < 1)
				return Infinity;
			else
			{
				char p1 = P.charAt(p-1);
				char p2 = P.charAt(p);
				char t1 = T.charAt(t-1);
				char t2 = T.charAt(t);

				if(p1 == t2 && p2 == t1)
					return 1;
				else
					return Infinity;
			}
		}
	}


	/**
	 * An MSDCollection object where the presented and transcribed texts are
	 * Vectors of Strings (viz. a list of words) where the individual elements
	 * considered in the MSD are the whole strings.  This is useful for
	 * calculating the word-level MSD.
	 */
	public static class WordCollection implements MSDCollection
	{
		Vector<String> P;
		Vector<String> T;
		boolean AllowTransposes = false;

		public WordCollection(Vector<String> PresentedData, Vector<String> TranscribedData)
		{
			P = PresentedData;
			T = TranscribedData;
		}

		public WordCollection(Vector<String> PresentedData,
				Vector<String> TranscribedData,
				boolean transposes)
		{
			P = PresentedData;
			T = TranscribedData;
			AllowTransposes = transposes;
		}

		//----------------------------------
		// implement the required interface
		//----------------------------------

		public int getPresentedSize()
		{
			return P.size();
		}

		public String getPresentedItem(int index)
		{
			return P.get(index);
		}

		public int getTranscribedSize()
		{
			return T.size();
		}

		public String getTranscribedItem(int index)
		{
			return T.get(index);
		}

		public int getItemDistance(int p, int t)
		{
			String Pstring = P.get(p);
			String Tstring = T.get(t);

			if(Pstring.equals(Tstring))
				return 0;
			else
				return 1;
		}

		public int getTransposedDistance(int p, int t)
		{
			if(!AllowTransposes || p < 0 || t < 0)
				return Infinity;
			else
			{
				String p1 = P.get(p-1);
				String p2 = P.get(p);
				String t1 = T.get(t-1);
				String t2 = T.get(t);

				if(p1.equals(t2) && p2.equals(t1))
					return 1;
				else
					return Infinity;
			}
		}
	}


	//-------------------------------------------------------------------
	// the MSDCollection that provides the data for this instance of MSD
	//-------------------------------------------------------------------

	MSDCollection C;


	/**
	 * This constructor takes an instance of a {@link MSD2.MSDCollection}
	 * object, and calculates the Minimum String Distance.
	 *
	 * @see MSD2.MSDCollection
	 * @param c is the source of data for the MSD algorithm
	 *        (a {@link MSD2.MSDCollection} object)
	 */
	public MSD2(MSDCollection c)
	{
		C = c;
		calculateD();
		findAlignments();
	}


	//--------------------------
	// convenience constructors
	//--------------------------

	/**
	 * This constructor takes two strings (the presented and transcribed
	 * strings) and constructs a MSD object, using the
	 * {@link MSD2.TwoStringCollection} implementation of the
	 * {@link MSD2.MSDCollection} interface.
	 *
	 * @see MSD2.TwoStringCollection
	 * @see MSD2.WordCollection
	 * @see MSD2.MSDCollection
	 * @see MSD2#MSD2
	 * @param PresentedText the presented text string
	 * @param TranscribedText the transcribed text string
	 * @param TraqnspositionsAllowed a flag the specifies whether the MSD
	 *        algorithm should allow transpositions (reversals of neighbouring
	 *        elements in the presented and transcribed text strings
	 */
	public MSD2(String PresentedText,
			String TranscribedText,
			boolean TranspositionsAllowed)
	{
		this(new TwoStringCollection(PresentedText,
				TranscribedText,
				TranspositionsAllowed));
	}

	/**
	 * This constructor takes two strings (the presented and transcribed
	 * strings) and constructs a MSD object, using the
	 * {@link MSD2.TwoStringCollection} implementation of the
	 * {@link MSD2.MSDCollection} interface.
	 *
	 * @see MSD2.TwoStringCollection
	 * @see MSD2.WordCollection
	 * @see MSD2.MSDCollection
	 * @see MSD2#MSD2
	 * @param PresentedText the presented text string
	 * @param TranscribedText the transcribed text string
	 */
	public MSD2(String PresentedText,
			String TranscribedText)
	{
		this(new TwoStringCollection(PresentedText,
				TranscribedText,
				false));
	}

	/**
	 * This constructor takes two <code>Vector</code>s of strings (the
	 * presented and transcribed data) and constructs a MSD object, using the
	 * {@link MSD2.WordCollection} implementation of the
	 * {@link MSD2.MSDCollection} interface.
	 *
	 * @see MSD2.WordCollection
	 * @see MSD2.TwoStringCollection
	 * @see MSD2.MSDCollection
	 * @see MSD2#MSD2
	 * @param PresentedText the presented text string
	 * @param TranscribedText the transcribed text string
	 * @param TraqnspositionsAllowed a flag the specifies whether the MSD
	 *        algorithm should allow transpositions (reversals of neighbouring
	 *        elements in the presented and transcribed text strings
	 */
	public MSD2(Vector<String> PresentedData,
			Vector<String> TranscribedData,
			boolean TranspositionsAllowed)
	{
		this(new WordCollection(PresentedData,
				TranscribedData,
				TranspositionsAllowed));
	}

	/**
	 * This constructor takes two <code>Vector</code>s of strings (the
	 * presented and transcribed data) and constructs a MSD object, using the
	 * {@link MSD2.WordCollection} implementation of the
	 * {@link MSD2.MSDCollection} interface.
	 *
	 * @see MSD2.WordCollection
	 * @see MSD2.TwoStringCollection
	 * @see MSD2.MSDCollection
	 * @see MSD2#MSD2
	 * @param PresentedText the presented text string
	 * @param TranscribedText the transcribed text string
	 */
	public MSD2(Vector<String> PresentedData,
			Vector<String> TranscribedData)
	{
		this(new WordCollection(PresentedData,
				TranscribedData,
				false));
	}


	//-----------------------------------------------------------------
	// These functions provide access to the MSDCollection information
	//-----------------------------------------------------------------

	/**
	 * This function returns the size of the presented text.
	 *
	 * @return the number of elements in the presented text.
	 */
	public int getPresentedSize()
	{
		return C.getPresentedSize();
	}

	/**
	 * Returns an element from the presented text.
	 *
	 * @param index indicates which element of the presented text is desired
	 * @return the requested element
	 */
	public String getPresentedItem(int index)
	{
		return C.getPresentedItem(index);
	}

	/**
	 * This function returns the size of the transcribed text.
	 *
	 * @return the number of elements in the transcribed text.
	 */
	public int getTranscribedSize()
	{
		return C.getTranscribedSize();
	}

	/**
	 * Returns a element from the transcribed text.
	 *
	 * @param index indicates which element of the transcribed text is desired
	 * @return the requested element
	 */
	public String getTranscribedItem(int index)
	{
		return C.getTranscribedItem(index);
	}


	//---------------------------------------------------------------------
	// The heart of the MSD algorithm is the D matrix.  These definitions
	// deal with calculating the D matrix.
	//---------------------------------------------------------------------

	int[][] D;   // the MSD 'D' matrix


	/**
	 * This function returns the minimum string distance (MSD) from the
	 * MSD matrix.  The MSD corresponds to the value in the bottom-right
	 * co-ordinate of the matrix.
	 *
	 * @return the MSD corresponding to the MSD matrix
	 */
	public int getMSD()
	{
		int x = D.length;
		int y = D[0].length;
		return D[x-1][y-1];
	}
	
	/**
	 * This function calculates the D matrix using the minimum string distance
	 * algorithm.  The base algorithm was taken from:
	 * <p>
	 *    Kruskal, JB. (1983) An overview of sequence comparison, time warps,
	 *    string edits, and macromolecules: The theory and practice of
	 *    sequence comparison, ed. Sankoff D, and Kruskal, JB.
	 * <p>
	 *
	 * This function uses the information in the {@link MSDCollection} object
	 * so this function can be applied to different sets of data (not just
	 * simple strings).
	 */
	private void calculateD()
	{
		int i, j;

		D = new int[getPresentedSize() + 1][getTranscribedSize() + 1];

		for(i = 0; i <= getPresentedSize(); i++)
			D[i][0] = i;

		for(j = 0; j <= getTranscribedSize(); j++)
			D[0][j] = j;

		for(i = 1; i <= getPresentedSize(); i++)
			for(j = 1; j <= getTranscribedSize(); j++)
			{
				int a, b, c, m;
				a = D[i-1][j] +1;
				b = D[i][j-1] +1;
				c = D[i-1][j-1] + C.getItemDistance(i-1, j-1);

				m = Math.min(a,b);
				m = Math.min(m,c);

				if((i-2 >= 0) && (j-2 >= 0))
				{
					int t = D[i-2][j-2] + C.getTransposedDistance(i-1, j-1);
					m = Math.min(m,t);
				}

				D[i][j] = m;
			}
	}


	//---------------------
	// the Alignment class
	//---------------------

	/**
	 * The purpose of this sub-class is to hold the information for one
	 * alignment.
	 */
	public class Alignment
	{
		String E;          // the explanation string

		Vector<String> P;  // the aligned presented text (a vector of Strings)
		Vector<String> T;  // the aligned transcribed text (a vector of Strings)

		boolean[][] Path;  // marks the alignment path through the MSD matrix

		public Alignment()
		{
			E = "";
			Path = null;
		}

		public Alignment(PlaceHolder ph)
		{
			E = ph.E;
			Path = null;
		}

		public String toString()
		{
			return E;
		}


		//-----------------------------------------------------------------------
		// This section calculates several incidental values, including the
		// aligned presented and transcribed text Vectors, and the Path
		// array.
		//
		// The explanation string is synonymous with a unique path through the
		// MSD 'D' matrix.  We use the explanation string to calculate the
		// aligned presented and transcribed data.  These are Vectors containing
		// the elements of the presented and transcribed data, that align with
		// the explanation string (the character '-' in inserted for missing
		// elements).  For example:
		//
		//    Presented Text:      aaa bbb  -  ddd fff ggg
		//    Transcribed Text:    aaa  -  ccc eee ggg fff
		//    Explanation String:   c   i   d   x   t   t
		//
		// The other data structure that is calculated is the path matrix.  This
		// matrix shadows the 'D' matrix, with a boolean value for every element
		// in D that indicates which elements in D comprise the alignment path.
		//-----------------------------------------------------------------------

		private void calc()
		{
			if(Path == null)
				calculatePresentedAndTranscribed();
		}

		private void calculatePresentedAndTranscribed()
		{
			int pres_index  = 0,
					trans_index = 0,
					i;

			int x = D.length;
			int y = D[0].length;

			Path = new boolean[x][y];
			Path[x-1][y-1] = true;

			P = new Vector<String>();
			T = new Vector<String>();

			for(i = 0; i < E.length(); i += 1)
			{
				String e = getExplanation(i);

				x = i - pres_index;
				y = i - trans_index;

				Path[x][y] = true;

				// c = correct
				if(e.equals("c"))
				{
					P.add(getPresentedItem(x));
					T.add(getTranscribedItem(y));
				}

				// i = insertion
				else if(e.equals("i"))
				{
					P.add("-"); pres_index += 1;
					T.add(getTranscribedItem(y));
				}

				// d = deletion
				else if(e.equals("d"))
				{
					P.add(getPresentedItem(x));
					T.add("-"); trans_index += 1;
				}

				// x = substitution
				else if(e.equals("x"))
				{
					P.add(getPresentedItem(x));
					T.add(getTranscribedItem(y));
				}

				// t = transposition
				else if(e.equals("t"))
				{
					P.add(getPresentedItem(x));
					T.add(getTranscribedItem(y));
				}
			}
		}


		//---------------------------------------------------------------------
		// Functions for accessing the aligned presented and transcribed texts
		//---------------------------------------------------------------------

		/**
		 * This function returns the size of the aligned presented text.
		 *
		 * @return the number of elements in the aligned presented text.
		 */
		public int getAlignedPresentedSize()
		{
			calc();
			return P.size();
		}

		/**
		 * Returns an element from the aligned presented text.
		 *
		 * @param index indicates the desired element of the aligned presented
		 *        text
		 * @return the requested element
		 */
		public String getAlignedPresentedItem(int index)
		{
			calc();
			return P.get(index);
		}

		/**
		 * This function returns the size of the aligned transcribed text.
		 *
		 * @return the number of elements in the aligned transcribed text.
		 */
		public int getAlignedTranscribedSize()
		{
			calc();
			return T.size();
		}

		/**
		 * Returns a element from the aligned transcribed text.
		 *
		 * @param index indicates the desired element of the aligned transcribed
		 *        text
		 * @return the requested element
		 */
		public String getAlignedTranscribedItem(int index)
		{
			calc();
			return P.get(index);
		}


		//-------------------------------------------------
		// Functions for accessing the Explanation strings
		//-------------------------------------------------

		/**
		 * Returns the length of the alignment string.
		 *
		 * @return the length of the alignment string.
		 */
		public int getExplanationLength()
		{
			return E.length();
		}

		/**
		 * Returns the whole alignment explanation string.  The explanation
		 * string is composed of the following characters:<p>
		 * <pre>
		 *    c = correct
		 *    i = insertion
		 *    d = deletion
		 *    x = substitution
		 *    t = transposition
		 * </pre>
		 *
		 * @return the Explanation string, composed of the characters above
		 */
		public String getExplanation()
		{
			return E;
		}

		/**
		 * Returns an explanation character from the alignment
		 * explanation string.
		 * <p>
		 * Valid characters in the alignment explanation string include:<p>
		 * <pre>
		 *    c = correct
		 *    i = insertion
		 *    d = deletion
		 *    x = substitution
		 *    t = transposition
		 * </pre>
		 *
		 * @param index indicates which element of the transcribed text is desired
		 * @return the requested element
		 */
		public String getExplanation(int index)
		{
			return "" + E.charAt(index);
		}


		//----------------------------------------------------------------
		// a function to print the MSD matrix with an alignment indicated
		//----------------------------------------------------------------

		/**
		 * This function prints the contents of the MSD matrix to standard out.
		 */
		public void printAlignmentMatrix()
		{
			String temp;
			int i, j;

			calc();     // if necessary, calculate the Path array

			int x = D.length;
			int y = D[0].length;

			// how wide do the columns have to be to accommodate
			// the values in the matrix?
			int spacing = 1;
			if(getMSD() > 9)
				spacing = 2;
			if(getMSD() > 99)
				spacing = 3;

			// find the length of the longest presented text item
			int longest_pres = 0;
			for(i = 0; i < getPresentedSize(); i += 1)
			{
				int temp_length = getPresentedItem(i).length();
				if(temp_length > longest_pres) longest_pres = temp_length;
			}

			// find the length of the longest transcribed text item
			int longest_trans = 0;
			for(j = 0; j < getTranscribedSize(); j += 1)
			{
				int temp_length = getTranscribedItem(j).length();
				if(temp_length > longest_trans) longest_trans = temp_length;
			}

			// output the letters of the presented text along the top
			for(j = 0; j < longest_pres; j += 1)
			{
				temp = padleftwithspaces("",
						longest_trans + spacing + spacing + 3) + "  ";

				for(i = 0; i < getPresentedSize(); i++)
				{
					String chr = " ",
							pres = getPresentedItem(i);

					if(pres.length() + j >= longest_pres)
						chr = "" + pres.charAt(j - longest_pres + pres.length());

					temp += padleftwithspaces(chr, spacing + 1) + "   ";
				}

				System.out.println(temp);
			}

			if(longest_pres > 1)
				System.out.println("");


			// output the bulk of the matrix
			int alignment_explanation_counter = 0;
			for(j = 0; j < y; j++)
			{
				// transcribed text down the left-hand-side
				temp = (j == 0 ? "" : getTranscribedItem(j-1));
				temp = padrightwithspaces(temp, longest_trans) + "  ";

				for(i = 0; i < x; i++)
				{
					String entry = padleftwithspaces("" + D[i][j], spacing);

					// if the corresponding element in A is true, then this element
					// is part of the alignment path, so wrap it in brakets
					if(Path[i][j] && (!(i == 0 && j == 0)) )
					{
						temp += " " + entry + "("
								+ getExplanation(alignment_explanation_counter) + ")";
						alignment_explanation_counter += 1;
					}
					else
						temp += " " + entry + "   ";
				}

				System.out.println(temp);
			}
		}
	}


	//--------------------------------------------------------------------
	// When the MSD calculations are performed, at least one alignment is
	// produced, and often there are multiple alignments.  This sections
	// helps us manage the alignments of this MSD calculation.
	//--------------------------------------------------------------------

	Vector<Alignment> A;    // a vector containing the Alignment objects

	/**
	 * This function returns a Vector containing the alignments
	 * (containing {@link Alignment} objects).
	 *
	 * @return the alignments for this presented and transcribed text strings
	 * @see Alignment
	 */
	public Vector<Alignment> getAlignmentVector()
	{
		return A;
	}

	/**
	 * This function returns the number of alignments.
	 *
	 * @return the number of alignments
	 * @see #getAlignment
	 */
	public int getAlignmentVectorSize()
	{
		return A.size();
	}

	/**
	 * Get one of the alignments.
	 *
	 * @param alignment indicates which alignment should be returned.
	 * @see #getAlignmentSize
	 * @see Alignment
	 */
	public Alignment getAlignment(int alignment)
	{
		return A.get(alignment);
	}

	/**
	 * Return the average alignment length.
	 *
	 * @return the average length of the alignments.
	 */
	public double getAverageAlignmentLength()
	{
		double retval = 0d;

		for(int i = 0; i < getAlignmentVectorSize(); i++)
			retval += getAlignment(i).getExplanationLength();
		return retval / (double)getAlignmentVectorSize();
	}


	//-----------------------------------------------------------
	// This section uses the D matrix to generate the alignments
	//-----------------------------------------------------------

	/*
	 * The purpose of this sub-class is to hold all of the data
	 * necessary to remember where we are, as we traverse the
	 * MSD matrix finding alignments.
	 */
	private class PlaceHolder
	{
		int X;      // the position in the D matrix
		int Y;
		String E;

		public PlaceHolder()
		{
			E = "";
			X = D.length - 1;           // bottom-right corner of D
			Y = D[0].length - 1;
		}

		public PlaceHolder(PlaceHolder ph, String e, int x, int y)
		{
			E = e + ph.E;
			X = x; Y = y;
		}

		Alignment getAlignment() { return new Alignment(this); };
	}


	/*
	 * This function generates a list of alignment strings, given
	 * the presented and transcribed text and the MSD matrix.
	 *
	 * The idea is to start in the bottom-right corner of the MSD matrix
	 * and find possible paths leading up and left to the top-left corner.
	 * As we proceed through the MSD matrix, occasionally we find a location
	 * where there are multiple possible paths to follow.  So we use a
	 * stack to remember where we are so we can continue from that point later
	 * without resorting to recursion.
	 */
	private void findAlignments()
	{
		// this is the result
		A = new Vector<Alignment>();

		// this is a stack that we'll use to remember where we are
		LinkedList<PlaceHolder> stack = new LinkedList<PlaceHolder>();

		// prime the stack with the start location
		// NB!  PlaceHolder() constructer defaults to bottom-right corner of D
		stack.addFirst(new PlaceHolder());

		while(stack.size() > 0)
		{
			// Certain sets of presented & transcribed text can cause the
			// number of alignments to grow exponentially.  This is a problem
			// only for long strings that are pathologically different from
			// one another, that are not of the same length.  When this
			// happens we're going to delete every second alignment.  This
			// should allow the alignment set to preserve some of its
			// descriptiveness, while at the same time limiting the growth to
			// a more managable quantity.
			if(stack.size() > MAX_ALIGNMENT_COUNT)
			{
				if(OUTPUT_PURGE_MESSAGES)
					System.err.println("'> " + MAX_ALIGNMENT_COUNT + "' in MSD, purging!");

				ListIterator<PlaceHolder> i = stack.listIterator();

				while(i.hasNext())
				{
					i.next();       // skip one

					if(i.hasNext())
					{
						i.next();    // delete next one
						i.remove();
					}
				}
			}


			// get the oldest element of the list
			PlaceHolder ph = stack.removeFirst();

			// the current position (x,y)
			int x = ph.X;
			int y = ph.Y;

			// if we've arrived at the top-left corner, we're done, so store
			// the alignment path
			if(x == 0 && y == 0)
			{
				A.add(ph.getAlignment());
				continue;
			}

			if(x > 0 && y > 0)
			{
				// correct (matching) characters
				if(D[x][y] == D[x-1][y-1] && C.getItemDistance(x-1,y-1) == 0)
					stack.addLast( new PlaceHolder(ph, "c", x-1, y-1) );

				// substitution error
				if(D[x][y] == D[x-1][y-1] + 1)
					stack.addLast( new PlaceHolder(ph, "x", x-1, y-1) );
			}

			// insertion error
			if(y > 0 && D[x][y] == D[x][y-1] + 1)
				stack.addLast( new PlaceHolder(ph, "i", x, y-1) );

			// deletion error
			if(x > 0 && D[x][y] == D[x-1][y] + 1)
				stack.addLast( new PlaceHolder(ph, "d", x-1, y) );

			// transposition error
			if((x-2 >= 0) && (y-2 >= 0)
					&&( D[x][y] - D[x-2][y-2] == C.getTransposedDistance(x-1, y-1)) )
				stack.addLast( new PlaceHolder(ph, "tt", x-2, y-2) );

		}  // while
	}


	//----------------------------------------
	// Functions used to print the MSD matrix
	//----------------------------------------

	/*
	 * This function pads the given text on the left with spaces
	 * (' ') out to the specified number of digits.  For example:
	 *           formatNumber("25", 5) = "   25"
	 *
	 * This function doesn't truncate numbers!
	 *           formatNumber("12345", 2) = "12345"
	 *
	 * Note that numberofdigits must be <= 20!!
	 */
	private static String padleftwithspaces(String number, int numberofdigits)
	{
		if(number.length() > numberofdigits) return number;

		String retval = "                    ";
		retval += number;
		return retval.substring(retval.length() - numberofdigits);
	}

	/*
	 * This function pads the given text on the right with spaces
	 * (' ') out to the specified number of digits.  For example:
	 *           padwithspaces("Shift", 10) = "Shift     "
	 *
	 * This function doesn't truncate!
	 *           padwithspaces("Shift", 2) = "Shift"
	 *
	 * Note that fieldwidth must be <= 20!!
	 */
	private static String padrightwithspaces(String text, int fieldwidth)
	{
		if(text.length() > fieldwidth) return text;

		return (text + "                    ").substring(0, fieldwidth);
	}

	/**
	 * This function prints the contents of the MSD matrix to standard out.
	 */
	public void printMSDMatrix()
	{
		String temp;
		int i, j;

		int x = D.length;
		int y = D[0].length;

		// how wide do the columns have to be to accommodate
		// the values in the matrix?
		int spacing = 1;
		if(getMSD() > 9)
			spacing = 2;
		if(getMSD() > 99)
			spacing = 3;

		// find the length of the longest presented text item
		int longest_pres = 0;
		for(i = 0; i < getPresentedSize(); i += 1)
		{
			int temp_length = getPresentedItem(i).length();
			if(temp_length > longest_pres) longest_pres = temp_length;
		}

		// find the length of the longest transcribed text item
		int longest_trans = 0;
		for(j = 0; j < getTranscribedSize(); j += 1)
		{
			int temp_length = getTranscribedItem(j).length();
			if(temp_length > longest_trans) longest_trans = temp_length;
		}

		// output the letters of the presented text along the top
		for(j = 0; j < longest_pres; j += 1)
		{
			temp = padleftwithspaces("",
					longest_trans + spacing + spacing) + "  ";

			for(i = 0; i < getPresentedSize(); i++)
			{
				String chr = " ",
						pres = getPresentedItem(i);

				if(pres.length() + j >= longest_pres)
					chr = "" + pres.charAt(j - longest_pres + pres.length());

				temp += padleftwithspaces(chr, spacing + 1);
			}

			System.out.println(temp);
		}

		if(longest_pres > 1)
			System.out.println("");

		// output the bulk of the matrix
		for(j = 0; j < y; j++)
		{
			// transcribed text down the left-hand-side
			temp = (j == 0 ? "" : getTranscribedItem(j-1));
			temp = padrightwithspaces(temp, longest_trans) + "  ";

			for(i = 0; i < x; i++)
				temp += padleftwithspaces("" + D[i][j], spacing + 1);

			System.out.println(temp);
		}
	}


	//-----------------------------------------------------------------
	// main provides an interaction mode for experimenting and testing
	//-----------------------------------------------------------------

	public static void main(String[] args) throws IOException
	{
		boolean transposition     = false;
		boolean word_level        = false;
		boolean output_alignments = false;

		for(int i = 0; i < args.length; i += 1)
		{
			if(args[i].equals("-t"))
				transposition = true;

			else if(args[i].equals("-w"))
				word_level = true;

			else if(args[i].equals("-a"))
				output_alignments = true;

			else
			{
				System.out.print(newline
						+ "Experiment.java - Text entry experiment and analysis software." + newline
						+ "              Version 0.0,  Janurary 5, 2004" + newline
						+ "    Copyright (C) by William Soukoreff and Scott MacKenzie" + newline
						+ "           Released under the GNU public licence" + newline
						+ newline
						+ "java MSD.java [-t] [-w] [-h] [-a]" + newline
						+ "  -h  = output this help text" + newline
						+ "  -t  = treat tranposition as an atomic edit" + newline
						+ "  -w  = perform the word-level MSD (defaults to character-level)" + newline
						+ "  -a  = display the alignment matrices" + newline
						);

				System.out.println(newline
						+ "This program allows you to experiment with the Minimum String Distance" + newline
						+ "algorithm interactively.  You are prompted for two strings (i.e. enter some" + newline
						+ "text followed by 'Enter', then enter another line of text followed by 'Enter'." + newline
						+ "The first string will be interpreted as the Presented Text, and the second" + newline
						+ "string as the Transcribed Text.  This program will calculate the MSD D-matrix" + newline
						+ "for the two strings, and find the number of alignments.  Each alignment is" + newline
						+ "displayed, followed by the D-matrix.  The contents of the D matrix are" + newline
						+ "identical for all of the alignments, although the path traversing the matrix" + newline
						+ "is different for each alignment.  The path through the D-matrix will be" + newline
						+ "indicated for each alignment." + newline
						);
				System.exit(1);
			}
		}

		BufferedReader stdin
		= new BufferedReader(new InputStreamReader(System.in), 1);

		while(true)
		{
			String p, t;

			System.out.println("");
			System.out.println("Enter the Presented Text:  (hit 'Enter' twice to exit)");
			p = stdin.readLine();
			if(p == null)
				break;

			System.out.println("Enter the Transcribed Text:");
			t = stdin.readLine();
			if(p.length() == 0 && t.length() == 0)
				break;

			MSD2 m;

			if(word_level)
			{
				StringTokenizer st = new StringTokenizer(p, " ");
				Vector<String> P = new Vector<String>();
				while(st.hasMoreTokens())
					P.add(st.nextToken());

				st = new StringTokenizer(t, " ");
				Vector<String> T = new Vector<String>();
				while(st.hasMoreTokens())
					T.add(st.nextToken());

				m = new MSD2(P, T, transposition);
			}
			else
				m = new MSD2(p, t, transposition);

			System.out.println("");
			System.out.println("Number of Alignments: " + m.getAlignmentVectorSize());
			System.out.println("MSD : " + m.getMSD());

			if(!output_alignments)
			{
				m.printMSDMatrix();
				System.out.println("");
			}

			int notmorethanten = Math.min(m.getAlignmentVectorSize(), 10);

			for(int i = 0; i < notmorethanten; i++)
			{
				Alignment a = m.getAlignment(i);

				if(!output_alignments)
				{
					System.out.println("Alignment " + i + " = " + a);
					continue;
				}

				System.out.println("");

				System.out.println("Alignment " + i + " = " + a);
				a.printAlignmentMatrix();
			}

			if(m.getAlignmentVectorSize() == 0)
				m.printMSDMatrix();
			else
			{
				System.out.print(           newline
						+ "  Legend"             + newline
						+ "    c = correct"      + newline
						+ "    i = insertion"    + newline
						+ "    d = deletion"     + newline
						+ "    x = substitution"
						);
				if(transposition)
					System.out.print(newline + "    t = transposition");
			}

			System.out.println("");
			System.out.println("-------------");
		}
	}
	
	public double getErrorRateNew() 
	{
	      return getMSD() / getAverageAlignmentLength() * 100.0;
	}
}



