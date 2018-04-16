//package default;
import java.util.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokiNawaSounds {
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

	private static final String[] forbiddenSyllables = new String[] {
		"ji", "ti", "wo", "wu"
	};

	private static final PrintStream o = System.out;

	private static boolean isForbiddenSyllable(String syl) {
		for(String forb : forbiddenSyllables) {
			if(forb.equals(syl)) {
				return true;
			}
		}
		return false;
	}

	public static void main(String[] args) {
		//generate all possible syllables
        //todo: find free sound space,
        // by subtracting extant words and their similar sounds from all the possible words
        //todo: record words with different harmonising vowels as similar
        Set<String> allPossibleWords = new TreeSet<String>();

		//firstly, generate initial-only syllables
		for(char c : vowels) {
			String s = String.valueOf(c);
			//o.println(s);
			//o.println(s+"n");
			wordInitialOnlySyllables.add(s);
			//wordInitialOnlySyllables.add(s+"n");
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
				//o.println(c+v);
				//syllables.add(c+v+"n");
				//o.println(c+v+"n");
			}
		}

		int numSingleSyllableWords = (syllables.size()+wordInitialOnlySyllables.size());
		int dualSyllableWords = numSingleSyllableWords * syllables.size();
		int tripleSyllableWords = dualSyllableWords * syllables.size();

		String allArgs = "";
		if(args.length > 0) {
			for(String arg : args) {
				allArgs += arg;
			}
		}
        //list all possible single-syllable words
		if(allArgs.contains("1") || allArgs.contains("U")) {
			//o.println("Word-initial syllables:");
			for(String s : wordInitialOnlySyllables) {
                if(allArgs.contains("1")) {
                    o.println(s);
                }
                if(allArgs.contains("U")) {
                    allPossibleWords.add(s);
                }
			}

			for(String t : syllables) {
				if(allArgs.contains("1")){
				    o.println(t);
                }
                if(allArgs.contains("U")) {
                    allPossibleWords.add(t);
                }
			}
			o.println("total possible single-syllable words: "+numSingleSyllableWords);
		}

		//list all possible double-syllable words
		if(allArgs.contains("2") || allArgs.contains("U")) {
			Set<String> wordInitialSyllables = new TreeSet<String>();
			for(String s : wordInitialOnlySyllables) {
				wordInitialSyllables.add(s);
			}
			for(String t : syllables) {
				wordInitialSyllables.add(t);
			}

			for(String firstSyllable : wordInitialSyllables) {
				for(String secondSyllable : syllables) {
					if(firstSyllable.endsWith("n") && secondSyllable.startsWith("n")) {
						//don't print syllables with 2 consecutive 'n's
						continue;
					}
					if(allArgs.contains("U")) {
					    allPossibleWords.add(firstSyllable+secondSyllable);
					}//*/
					if(allArgs.contains("2")) {
                        o.println(firstSyllable + secondSyllable);
                    }
				}
			}
			o.println("total possible dual-syllable words:"+dualSyllableWords);
		}

        //list all possible triple-syllable words
        if(allArgs.contains("3") || allArgs.contains("U")) {
            Set<String> wordInitialSyllables = new TreeSet<String>();
            for(String s : wordInitialOnlySyllables) {
                wordInitialSyllables.add(s);
            }
            for(String t : syllables) {
                wordInitialSyllables.add(t);
            }

            for(String firstSyllable : wordInitialSyllables) {
                for(String secondSyllable : syllables) {
                    for(String thirdSyllable : syllables) {
                        if(firstSyllable.endsWith("n") && secondSyllable.startsWith("n")) {
                            //don't print syllables with 2 consecutive 'n's
                            continue;
                        }
                        /*if(allArgs.contains("U")) {
                            allPossibleWords.add(firstSyllable+secondSyllable+thirdSyllable);
                        }//*/
                        if(allArgs.contains("3")) {
                            o.println(firstSyllable + secondSyllable + thirdSyllable);
                        }
                    }
                }
            }
            o.println("total possible triple-syllable words:"+tripleSyllableWords);
        }

		//print unused sounds which aren't too similar to existing words
        if(allArgs.contains("U")) {
            String[] dict = scrapeWordsFromDictionary(dictionaryFile);
            for(String word : dict) {
                String[] similarWords = similarWordsTo(word);
                allPossibleWords.remove(word);
                //o.println(word+" : "+
                for(String similarWord : similarWords) {
                    allPossibleWords.remove(similarWord);
                }
            }
            o.println("unused words:");
            for(String wurd : allPossibleWords) {
                o.println(wurd);
            }
            o.println("total: "+allPossibleWords.size());
        }

		//lint dictionary.md
		if (args.length == 0) {
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
                //check for exact duplicate words
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
	}

	public static String[] similarWordsTo(String word) {
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
            //if(i != 0) {//if it's not the first letter
                if (word.charAt(i) == 't') {//replace t with k
                    similarWords.add(replaceCharAt(word, i, 'k'));
                    similarWords.add(replaceCharAt(word, i, 'p'));
                }
                if (word.charAt(i) == 'k') {//replace k with t
                    similarWords.add(replaceCharAt(word, i, 't'));
                    similarWords.add(replaceCharAt(word, i, 'p'));
                }
                /*if (word.charAt(i) == 'p') {//replace k with t
                    similarWords.add(replaceCharAt(word, i, 't'));
                    similarWords.add(replaceCharAt(word, i, 'k'));
                }//*/
            //}
        }

        String[] ret = new String[similarWords.size()];
        return similarWords.toArray(ret);
    }

    private static String replaceCharAt(String victim, int index, char replacement) {
        StringBuilder myName = new StringBuilder(victim);
        myName.setCharAt(index, replacement);
        return myName.toString();
    }

	public static String[] scrapeWordsFromDictionary(File dictFile) {
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
    public static String fileToString(File f) {
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
