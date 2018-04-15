//package default;
import java.util.*;
import java.io.*;
public class TokiNawaSounds
{
	private static final char[] consonants = "jkmnpstw".toCharArray();
	//private static final char[] consonants = "jklmnpstw".toCharArray();
	private static final char[] vowels = "aiou".toCharArray();
	//private static final char[] vowels = "aeiou".toCharArray();

	private static final int maxSyllables = 3;
	private static Set<String> syllables = new TreeSet<String>();
	private static Set<String> wordInitialOnlySyllables = new TreeSet<String>();

	private static final String[] forbiddenSyllables = new String[] {
		"ji", "ti", "wo", "wu"
	};

	private static final PrintStream o = System.out;

	private static boolean isForbiddenSyllable(String syl)
	{
		for(String forb : forbiddenSyllables)
		{
			if(forb.equals(syl))
			{
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args)
	{
		//generate all possible syllables

		//firstly, generate initial-only syllables
		for(char c : vowels)
		{
			String s = String.valueOf(c);
			//o.println(s);
			//o.println(s+"n");
			wordInitialOnlySyllables.add(s);
			wordInitialOnlySyllables.add(s+"n");
		}

		//now, generate all other possible syllables
		for(char cc : consonants)
		{
			String c = String.valueOf(cc);
			for(char vv : vowels)
			{
				String v = String.valueOf(vv);
				if(isForbiddenSyllable(c+v))
				{//if it's a forbidden syllable, skip adding it to the list
					continue;
				}
				syllables.add(c+v);
				//o.println(c+v);
				syllables.add(c+v+"n");
				//o.println(c+v+"n");
			}
		}

		int numSingleSyllableWords = (syllables.size()+wordInitialOnlySyllables.size());
		int dualSyllableWords = numSingleSyllableWords * syllables.size();

		String allArgs = "";
		if(args.length > 0)
		{
			for(String arg : args) {
				allArgs += arg;
			}
		}

		if(allArgs.contains("1"))
		{
			//o.println("Word-initial syllables:");
			for(String s : wordInitialOnlySyllables) {
				o.println(s);
			}

			for(String t : syllables)
			{
				o.println(t);
			}
			o.println("total possible single-syllable words: "+numSingleSyllableWords);
		}

		if(allArgs.contains("2"))
		{
			Set<String> wordInitialSyllables = new TreeSet<String>();
			for(String s : wordInitialOnlySyllables)
			{
				wordInitialSyllables.add(s);
			}
			for(String t : syllables)
			{
				wordInitialSyllables.add(t);
			}

			for(String firstSyllable : wordInitialSyllables)
			{
				for(String secondSyllable : syllables)
				{
					if(firstSyllable.endsWith("n") && secondSyllable.startsWith("n")) {
						//don't print syllables with 2 consecutive 'n's
						continue;
					}
					o.println(firstSyllable+secondSyllable);
				}
			}
			o.println("possible dual-syllable words:"+dualSyllableWords);
		}

		for(int currentSyllable = 1; currentSyllable <= maxSyllables; currentSyllable++)
		{

		}
	}

	/**Reads the supplied (plaintext) file as a string and returns it.
     * @File f the supplied file. This MUST be a plaintext file.
     * @return the contents of the file, as a String.*/
    public static String fileToString(File f)
    {
        if(!f.isFile())
        {
            throw new IllegalArgumentException("Supplied File object must represent an actual file.");
        }
        try
        {
            FileReader fr = new FileReader(f);
            char[] tmp = new char[(int)f.length()];
            char c;
            int j = 0;
            for(int i = fr.read(); i != -1; i = fr.read())
            {
                c = (char)i;
                tmp[j] = c;
                j++;
            }
            fr.close();
            String ret = new String(tmp);
            return ret;
        }
        catch(Exception e)
        {
            System.err.println("failed to read file: \""+f.getName()+"\"!");
            e.printStackTrace();
            return "";
        }
	}
}
