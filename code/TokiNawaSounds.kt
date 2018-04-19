//package default;

import com.medavox.util.validate.Validator
import java.util.*

import java.io.*
import java.util.regex.Pattern
class TokiNawaSounds {
    private val consonantsString = "jkmnpstw"
    private val vowelsString = "aiu"
    private val consonants = consonantsString.toCharArray()
    private val vowels = vowelsString.toCharArray()
    private val allowSyllableFinalN = false

    //private static final char[] consonants = "jklmnpstw".toCharArray();
    //private static final char[] vowels = "aeiou".toCharArray();
    //private static final File dictionaryFile = new File("sorted.md");
    private val dictionaryFile = File("dictionary.md")

    private val maxSyllables = 3
    private val syllables = TreeSet<String>()
    private val wordInitialOnlySyllables = TreeSet<String>()

    private val wordInitialSyllables = TreeSet<String>()
    private val allPossibleWords = TreeSet<String>()

    private val forbiddenSyllables = arrayOf("ji", "ti", "wo", "wu")

    internal val o = System.out
    internal val e = System.err

    private val commands:Array<String> = arrayOf("unused", "syllables", "lint")

    /**list unused potential words which aren't too similar to existing words */
    private fun listUnusedPotentialWords(wordsFromDictionary: Array<String>): String {
        val b = StringBuilder()
        //String[] wordsFromDictionary = scrapeWordsFromDictionary(dictionaryFile);
        for (word in wordsFromDictionary) {
            val similarWords = similarWordsTo(word)
            allPossibleWords.remove(word)
            //o.println(word+" : "+
            for (similarWord in similarWords) {
                allPossibleWords.remove(similarWord)
            }
        }
        o.println("unused words:")
        for (wurd in allPossibleWords) {
            //o.println(wurd);
            b.append(wurd).append("\n")
        }
        o.println("total: " + allPossibleWords.size)
        return b.toString()
    }

    private fun isForbiddenSyllable(syl: String): Boolean {
        for (forb in forbiddenSyllables) {
            if (forb == syl) {
                return true
            }
        }
        return false
    }

    fun main(args: Array<String>) {
        //todo: record words with different harmonising vowels as similar
        try {
            Validator.check(args.size == 2, "must have 2 arguments")
            val f = File(args[0])
            Validator.check(f.isDirectory, "supplied path must be a directory")


        } catch (ex: Exception) {
            e.println("call failed, with exception: " + ex)
        }
        //generate all possible syllables

        //firstly, generate initial-only syllables
        for (c in vowels) {
            wordInitialOnlySyllables.add(c.toString())
        }

        //then, generate all other possible syllables
        for (cc in consonants) {
            val c = cc.toString()
            for (vv in vowels) {
                val v = vv.toString()
                if (isForbiddenSyllable(c + v)) {
                    //if it's a forbidden syllable, skip adding it to the list
                    continue
                }
                syllables.add(c + v)
            }
        }

        wordInitialSyllables += wordInitialOnlySyllables
        wordInitialSyllables += syllables

        val numSingleSyllableWords = wordInitialSyllables.size
        val dualSyllableWords = numSingleSyllableWords * syllables.size
        val tripleSyllableWords = dualSyllableWords * syllables.size

        e.println("possible single-syllable words:" + numSingleSyllableWords)
        e.println("possible double-syllable words:" + dualSyllableWords)
        e.println("possible triple-syllable words:" + tripleSyllableWords)

        //lint dictionary.md
        if (args.size == 0) {
            lintTheDictionrary(dictionaryFile)
        }
    }

    /**list all possible words, up to triple-syllable words */
    private fun listUpToTripleSyllableWords(syllableCount: Int,
                                            populateAllWords: Boolean): String {
        if (syllableCount != 1 && syllableCount != 2 && syllableCount != 3) {
            throw IllegalArgumentException("syllable count must be 1, 2 or 3. " +
                    "Passed value: " + syllableCount)
        }
        val ones = StringBuilder()
        val twos = StringBuilder()
        val tris = StringBuilder()
        for (firstSyllable in wordInitialSyllables) {
            if (populateAllWords) {
                allPossibleWords.add(firstSyllable)
            }
            ones.append(firstSyllable).append("\n")
            for (secondSyllable in syllables) {
                if (syllableCount < 2) {
                    break
                }
                if (firstSyllable.endsWith("n") && secondSyllable.startsWith("n")) {
                    //don't print syllables with 2 consecutive 'n's
                    continue
                }
                twos.append(firstSyllable).append(secondSyllable).append("\n")
                if (populateAllWords) {
                    allPossibleWords.add(secondSyllable)
                }
                for (thirdSyllable in syllables) {
                    if (syllableCount < 3) {
                        break
                    }
                    if (secondSyllable.endsWith("n") && thirdSyllable.startsWith("n")) {
                        //don't print syllables with 2 consecutive 'n's
                        continue
                    }
                    if (populateAllWords) {
                        allPossibleWords.add(firstSyllable + secondSyllable + thirdSyllable)
                    }//*/

                    tris.append(firstSyllable).append(secondSyllable).append(thirdSyllable)
                            .append("\n")
                }
            }
        }
        return ones.toString() + twos.toString() + tris.toString()
    }

    private fun lintTheDictionrary(dictionary: File) {
        val dict = scrapeWordsFromDictionary(dictionaryFile)
        val dupCheck = TreeSet<String>()
        var complaints = 0
        for (word in dict) {
            //check for illegal letters
            val invalidLetters = word
                    .replace("[$vowelsString$consonantsString]".toRegex(), "")
            if (invalidLetters.length > 0) {
                o.println("word \"$word\" contains illegal letters: $invalidLetters")
                complaints++
            }

            //check for any of the 4 illegal syllables
            for (forb in forbiddenSyllables) {
                if (word.contains(forb)) {
                    o.println("word \"$word\" contains illegal syllable \"$forb\"")
                    complaints++
                }
            }

            //check for syllable-final Ns
            if (!allowSyllableFinalN) {
                if (word.replace("n", "").length < word.length) {
                    var i = word.indexOf("n", 0)
                    while (i != -1) {
                        //o.println("i:"+i);
                        if (i != word.length - 1 && !vowelsString.contains(word[i + 1].toString())) {
                            //if the letter after our n is not a vowel,
                            o.println("word \"$word\" contains an N before another consonant")
                            complaints++
                        }
                        i = word.indexOf("n", i + 1)
                    }
                }
            }
            //check for exact-duplicate words
            if (dupCheck.contains(word)) {
                o.println("word \"$word\" already exists")
                complaints++
            } else {
                dupCheck.add(word)
            }

            //check for similar words
            val similarWords = similarWordsTo(word)
            for (similarWord in similarWords) {
                allPossibleWords.remove(similarWord)
                for (otherWord in dict) {
                    if (otherWord == similarWord) {
                        o.println("word \"$word\" is very similar to \"$otherWord\"")
                        complaints++
                    }
                }
            }
        }
        o.println("total complaints: " + complaints)
    }


    private fun similarWordsTo(word: String): Array<String> {
        if (word.length == 1) {
            return arrayOf()
        }
        val similarWords = LinkedList<String>()
        for (i in 0 until word.length) {
            val charAt = word[i].toString()

            //replace all vowels with all other vowels
            if (vowelsString.contains(charAt)) {//if this char is a vowel
                for (vowel in vowels) {
                    if (vowel != word[i]) {
                        val replaced = word.toCharArray()
                        replaced[i] = vowel
                        similarWords.add(String(replaced))
                    }
                }
            }
            if (word[i] == 'm') {//replace m with n
                similarWords.add(replaceCharAt(word, i, 'n'))
            }
            if (word[i] == 'n') {
                if (i != word.length - 1) {//replace non-final n with m
                    similarWords.add(replaceCharAt(word, i, 'm'))
                } else {
                    similarWords.add(word.substring(0, word.length - 1))
                }
                if (i == word.length - 2) {//if there's a penultimate n, remove the final vowel
                    similarWords.add(word.substring(0, word.length - 1))
                }
            }
            if (word[i] == 't') {//replace t with k
                similarWords.add(replaceCharAt(word, i, 'k'))
                similarWords.add(replaceCharAt(word, i, 'p'))
            }
            if (word[i] == 'k') {//replace k with t
                similarWords.add(replaceCharAt(word, i, 't'))
                similarWords.add(replaceCharAt(word, i, 'p'))
            }
        }

        //val ret = arrayOfNulls<String>(similarWords.size)
        return similarWords.toTypedArray()
    }

    private fun replaceCharAt(victim: String, index: Int, replacement: Char): String {
        val myName = StringBuilder(victim)
        myName.setCharAt(index, replacement)
        return myName.toString()
    }

    private fun scrapeWordsFromDictionary(dictFile: File): Array<String> {
        //String wholeDict = fileToString(new File("dictionary.md"));
        val wholeDict = fileToString(dictFile)
        val byLine = wholeDict.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val words = arrayOfNulls<String>(byLine.size)
        var validElements = 0
        for (i in 2 until byLine.size) {//start after the table heading
            val count = byLine[i].length - byLine[i].replace("|", "").length
            if (count != 5) {//if there aren't 5 pipes on the line, it's not a table row
                continue
            }
            //o.println("line: "+byLine[i]);
            val pat = Pattern.compile("([a-z]+) *\\|.*")
            val mat = pat.matcher(byLine[i])
            //o.println("group count: "+mat.groupCount());
            //o.println("group :"+mat.group());
            //o.println("matches: "+mat.matches());
            words[i] = mat.replaceAll("$1")
            validElements++
        }
        var nextValidIndex = 0
        val noNulls: Array<String> = Array<String>(validElements, {""})
        for (s :String? in words) {
            if (s != null) {
                noNulls[nextValidIndex] = s
                nextValidIndex++
            }
        }
        return noNulls
    }

    /**Reads the supplied (plaintext) file as a string and returns it.
     * @param f the supplied file. This MUST be a plaintext file.
     * @return the contents of the file, as a String.
     */
    private fun fileToString(f: File): String {
        if (!f.isFile) {
            throw IllegalArgumentException("Supplied File object must represent an actual file.")
        }
        try {
            val fr = FileReader(f)
            val tmp = CharArray(f.length().toInt())
            var c: Char
            var j = 0
            var i = fr.read()
            while (i != -1) {
                c = i.toChar()
                tmp[j] = c
                j++
                i = fr.read()
            }
            fr.close()
            return String(tmp)
        } catch (e: Exception) {
            System.err.println("failed to read file: \"" + f.name + "\"!")
            e.printStackTrace()
            return ""
        }

    }
}
