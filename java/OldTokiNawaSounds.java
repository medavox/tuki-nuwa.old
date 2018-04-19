//package default;

import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokiNawaSoundsOld {
    private static final String consonantsString = "jkmnpstw";
    private static final String vowelsString = "aiu";
    private static final char[] consonants = consonantsString.toCharArray();
    private static final char[] vowels = vowelsString.toCharArray();
    private static boolean allowSyllableFinalN = false;
    //private static final char[] consonants = "jklmnpstw".toCharArray();
    //private static final char[] vowels = "aeiou".toCharArray();

    //private static final File dictionaryFile = new File("sorted.md");
    private static final File dictionaryFile = new File("dictionary.md");

	private static final int maxSyllables = 3;
	private static Set<String> syllables = new TreeSet<String>();
	private static Set<String> wordInitialOnlySyllables = new TreeSet<String>();

    private static Set<String> wordInitialSyllables = new TreeSet<String>();
    private static Set<String> allPossibleWords = new TreeSet<String>();

	private static final String[] forbiddenSyllables = new String[] {
		"ji", "ti", "wo", "wu"
	};

	static final PrintStream o = System.out;
	static final PrintStream e = System.err;

	private static boolean isForbiddenSyllable(String syl) {
		for(String forb : forbiddenSyllables) {
			if(forb.equals(syl)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
        //todo: record words with different harmonising vowels as similar

    //generate all possible syllables

		//firstly, generate initial-only syllables
		for(char c : vowels) {
			wordInitialOnlySyllables.add(String.valueOf(c));
		}

		//then, generate all other possible syllables
		for(char cc : consonants) {
			String c = String.valueOf(cc);
			for(char vv : vowels) {
				String v = String.valueOf(vv);
				if(isForbiddenSyllable(c+v)) {
                    //if it's a forbidden syllable, skip adding it to the list
					continue;
				}
				syllables.add(c+v);
			}
		}

        for(String s : wordInitialOnlySyllables) {
            wordInitialSyllables.add(s);
        }
        for(String t : syllables) {
            wordInitialSyllables.add(t);
        }

        int numSingleSyllableWords = wordInitialSyllables.size();
        int dualSyllableWords = numSingleSyllableWords * syllables.size();
        int tripleSyllableWords = dualSyllableWords * syllables.size();

        e.println("possible single-syllable words:"+numSingleSyllableWords);
		e.println("possible double-syllable words:"+dualSyllableWords);
		e.println("possible triple-syllable words:"+tripleSyllableWords);

		//lint dictionary.md
		if (args.length == 0) {
		    lintTheDictionrary(dictionaryFile);
        }
	}

	/**list unused potential words which aren't too similar to existing words*/
	private String listUnusedPotentialWords(String[] wordsFromDictionary) {
	    StringBuilder b = new StringBuilder();
        //String[] wordsFromDictionary = scrapeWordsFromDictionary(dictionaryFile);
        for(String word : wordsFromDictionary) {
            String[] similarWords = similarWordsTo(word);
            allPossibleWords.remove(word);
            //o.println(word+" : "+
            for(String similarWord : similarWords) {
                allPossibleWords.remove(similarWord);
            }
        }
        o.println("unused words:");
        for(String wurd : allPossibleWords) {
            //o.println(wurd);
            b.append(wurd).append("\n");
        }
        o.println("total: "+allPossibleWords.size());
        return b.toString();
    }

    /**list all possible words, up to triple-syllable words*/
    private static String listUpToTripleSyllableWords(final int syllableCount,
                                                      boolean populateAllWords) {
        if(syllableCount != 1 && syllableCount != 2 && syllableCount != 3) {
            throw new IllegalArgumentException("syllable count must be 1, 2 or 3. " +
                    "Passed value: "+syllableCount);
        }
        StringBuilder ones = new StringBuilder();
        StringBuilder twos = new StringBuilder();
        StringBuilder tris = new StringBuilder();
        for(String firstSyllable : wordInitialSyllables) {
            if(populateAllWords) {
                allPossibleWords.add(firstSyllable);
            }
            ones.append(firstSyllable).append("\n");
            for(String secondSyllable : syllables) {
                if( syllableCount < 2) {
                    break;
                }
                if(firstSyllable.endsWith("n") && secondSyllable.startsWith("n")) {
                    //don't print syllables with 2 consecutive 'n's
                    continue;
                }
                twos.append(firstSyllable).append(secondSyllable).append("\n");
                if(populateAllWords) {
                    allPossibleWords.add(secondSyllable);
                }
                for(String thirdSyllable : syllables) {
                    if(syllableCount < 3) {
                        break;
                    }
                    if(secondSyllable.endsWith("n") && thirdSyllable.startsWith("n")) {
                        //don't print syllables with 2 consecutive 'n's
                        continue;
                    }
                    if(populateAllWords) {
                        allPossibleWords.add(firstSyllable+secondSyllable+thirdSyllable);
                    }//*/

                    tris.append(firstSyllable).append(secondSyllable).append(thirdSyllable)
                    .append("\n");
                }
            }
        }
        return ones.toString() + twos.toString() + tris.toString();
    }

	private static void lintTheDictionrary(File dictionary) {
        String[] dict = scrapeWordsFromDictionary(dictionaryFile);
        Set<String> dupCheck = new TreeSet<>();
        int complaints = 0;
        for(String word : dict) {
            //check for illegal letters
            String invalidLetters = word
                    .replaceAll("["+vowelsString+consonantsString+"]", "");
            if(invalidLetters.length() > 0) {
                o.println("word \""+word+"\" contains illegal letters: "+invalidLetters);
                complaints++;
            }

            //check for any of the 4 illegal syllables
            for(String forb : forbiddenSyllables) {
                if(word.contains(forb)) {
                    o.println("word \""+word+"\" contains illegal syllable \""+forb+"\"");
                    complaints++;
                }
            }

            //check for syllable-final Ns
            if(!allowSyllableFinalN) {
                if(word.replace("n", "").length() < word.length()) {
                    for(int i = word.indexOf("n", 0);
                        i != -1;
                        i = word.indexOf("n", i+1)) {
                        //o.println("i:"+i);
                        if(i != word.length()-1 &&
                                !vowelsString.contains(String.valueOf(word.charAt(i+1)))) {
                            //if the letter after our n is not a vowel,
                            o.println("word \""+word+"\" contains an N before another consonant");
                            complaints++;
                        }
                    }
                }
            }
            //check for exact-duplicate words
            if(dupCheck.contains(word)) {
                o.println("word \""+word+"\" already exists");
                complaints++;
            }else {
                dupCheck.add(word);
            }

            //check for similar words
            String[] similarWords = similarWordsTo(word);
            for(String similarWord : similarWords) {
                allPossibleWords.remove(similarWord);
                for(String otherWord : dict) {
                    if(otherWord.equals(similarWord)) {
                        o.println("word \""+word+"\" is very similar to \""+otherWord+"\"");
                        complaints++;
                    }
                }
            }
        }
        o.println("total complaints: "+complaints);
    }


	private static String[] similarWordsTo(String word) {
	    if(word.length() == 1) {
	        return new String[]{};
        }
	    List<String> similarWords = new LinkedList<String>();
	    for(int i = 0; i < word.length(); i++) {
	        String charAt = String.valueOf(word.charAt(i));

	        //replace all vowels with all other vowels
	        if(vowelsString.contains(charAt)) {//if this char is a vowel
	            for(char vowel : vowels) {
	                if(vowel != word.charAt(i)) {
	                    char[] replaced = word.toCharArray();
	                    replaced[i] = vowel;
	                    similarWords.add(String.valueOf(replaced));
                    }
                }
            }
            if(word.charAt(i) == 'm') {//replace m with n
                similarWords.add(replaceCharAt(word, i, 'n'));
            }
            if(word.charAt(i) == 'n') {
                if(i != word.length()-1) {//replace non-final n with m
                    similarWords.add(replaceCharAt(word, i, 'm'));
                }
                else {
                    similarWords.add(word.substring(0, word.length()-1));
                }
                if(i == word.length()-2) {//if there's a penultimate n, remove the final vowel
                    similarWords.add(word.substring(0, word.length()-1));
                }
            }
            if (word.charAt(i) == 't') {//replace t with k
                similarWords.add(replaceCharAt(word, i, 'k'));
                similarWords.add(replaceCharAt(word, i, 'p'));
            }
            if (word.charAt(i) == 'k') {//replace k with t
                similarWords.add(replaceCharAt(word, i, 't'));
                similarWords.add(replaceCharAt(word, i, 'p'));
            }
        }

        String[] ret = new String[similarWords.size()];
        return similarWords.toArray(ret);
    }

    private static String replaceCharAt(String victim, int index, char replacement) {
        StringBuilder myName = new StringBuilder(victim);
        myName.setCharAt(index, replacement);
        return myName.toString();
    }

	private static String[] scrapeWordsFromDictionary(File dictFile) {
        //String wholeDict = fileToString(new File("dictionary.md"));
        String wholeDict = fileToString(dictFile);
        String[] byLine = wholeDict.split("\n");
        String[] words = new String[byLine.length];
        int validElements = 0;
        for(int i = 2; i < byLine.length; i++) {//start after the table heading
            int count = byLine[i].length() - byLine[i].replace("|", "").length();
            if(count != 5) {//if there aren't 5 pipes on the line, it's not a table row
                continue;
            }
            //o.println("line: "+byLine[i]);
            Pattern pat = Pattern.compile("([a-z]+) *\\|.*");
            Matcher mat = pat.matcher(byLine[i]);
            //o.println("group count: "+mat.groupCount());
            //o.println("group :"+mat.group());
            //o.println("matches: "+mat.matches());
            words[i] = mat.replaceAll("$1");
            validElements++;
        }
        int nextValidIndex = 0;
        String[] noNulls = new String[validElements];
        for(int i = 0; i < words.length; i++) {
            if(words[i] != null) {
                noNulls[nextValidIndex] = words[i];
                nextValidIndex++;
            }
        }
        return noNulls;
    }

	/**Reads the supplied (plaintext) file as a string and returns it.
     * @param f the supplied file. This MUST be a plaintext file.
     * @return the contents of the file, as a String.*/
    private static String fileToString(File f) {
        if(!f.isFile()) {
            throw new IllegalArgumentException("Supplied File object must represent an actual file.");
        }
        try {
            FileReader fr = new FileReader(f);
            char[] tmp = new char[(int)f.length()];
            char c;
            int j = 0;
            for(int i = fr.read(); i != -1; i = fr.read()) {
                c = (char)i;
                tmp[j] = c;
                j++;
            }
            fr.close();
            String ret = new String(tmp);
            return ret;
        }
        catch(Exception e) {
            System.err.println("failed to read file: \""+f.getName()+"\"!");
            e.printStackTrace();
            return "";
        }
	}
}
