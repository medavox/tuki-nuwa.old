//package default;

import java.util.*

import java.io.*
import java.util.regex.Pattern

private val e = System.err
private val o = System.out

/**
I will definitely not add sounds (phonemes) to the phonology;
I don't know enough about linguistic typology to choose new sounds
(or new phonotactic rules) that are as easy for all humans to pronounce.

Instead, I'm merely using the raw material given to me
by Toki Pona's phonology and phonotactics
to construct new words.

 This file helps with the boilerplate maths of it all:

 * given 3 vowels and 10 consonants, how many possible single-syllable words are there?<br />
 * how many possible 2-syllable words?<br />
 * and 3-syllable words?<br />
<br />
 * Do all the words in the dictionary follow the phonological rules (as they currently stand)?

 * Given the words in the dictionary, what are some unused sounds that could be used for new words?
 */

fun main(args: Array<String>) {
    if(args.size == 2) {
        val t = TokiNawaSounds()
        val command: String = args[0].toLowerCase()
        if("[0-9]".toRegex().matches(args[1]) &&
                (command.equals("syllables") || command.equals("s")) ) {
            //print syllables
            for(s:String in t.listUpToTripleSyllableWords(args[1].toInt())){
                o.println(s)
            }
        }
        else {
            //the other two commands require a second argument of a dictionary file,
            //so parse it as that
            //args[0].equals("lint")
            val dict: File = File(args[1])
            if(!dict.exists() || !dict.isFile || !dict.canRead() || dict.length() < 5) {
                e.println("invalid file specified.")
            }else {
                val dictContents = t.scrapeWordsFromDictionary(dict)
                if(command.equals("lint" ) || command.equals("l")) {
                    t.lintTheDictionary(dictContents)
                }
                else if(command.equals("unused") || command.equals("u")) {
                    //populate the list of all potential words,
                    // then subtract all the dictionary words (and similar) from it
                    o.println(t.listUnusedPotentialWords(dictContents,
                            t.listUpToTripleSyllableWords(2).toMutableSet()))
                }
            }
        }
    }
    else {
        e.println("nope!")
    }
}

class TokiNawaSounds {
    private val consonantsString = "hjklmnpstw"
    private val vowelsString = "aiu"
    private val consonants = consonantsString.toCharArray()
    private val vowels = vowelsString.toCharArray()
    private val allowSyllableFinalN = false

    //private static final char[] consonants = "jklmnpstw".toCharArray();
    //private static final char[] vowels = "aeiou".toCharArray();
    //private static final File dictionaryFile = new File("sorted.md");

    //private val dictionaryFile = File("dictionary.md")

    private val syllables = TreeSet<String>()
    private val wordInitialOnlySyllables = TreeSet<String>()

    private val wordInitialSyllables = TreeSet<String>()


    private val forbiddenSyllables = arrayOf("ji", "ti", "wo", "wu", "hu")


    private val commands:Array<String> = arrayOf("unused", "syllables", "lint")

    private fun isForbiddenSyllable(syl: String): Boolean {
        for (forb in forbiddenSyllables) {
            if (forb == syl) {
                return true
            }
        }
        return false
    }

    init{
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

        e.println("possible single-syllable words: $numSingleSyllableWords")
        e.println("possible double-syllable words: $dualSyllableWords")
        e.println("possible triple-syllable words: $tripleSyllableWords")
    }

    /**list all possible words, up to triple-syllable words */
    internal fun listUpToTripleSyllableWords(syllableCount: Int = 3): Set<String> {
        if (syllableCount != 1 && syllableCount != 2 && syllableCount != 3) {
            throw IllegalArgumentException("syllable count must be 1, 2 or 3. " +
                    "Passed value: " + syllableCount)
        }
        val allPossibleWords = TreeSet<String>()
        val ones = StringBuilder()
        val twos = StringBuilder()
        val tris = StringBuilder()
        for (firstSyllable in wordInitialSyllables) {
            allPossibleWords.add(firstSyllable)
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
                allPossibleWords.add(secondSyllable)
                for (thirdSyllable in syllables) {
                    if (syllableCount < 3) {
                        break
                    }
                    if (secondSyllable.endsWith("n") && thirdSyllable.startsWith("n")) {
                        //don't print syllables with 2 consecutive 'n's
                        continue
                    }
                    allPossibleWords.add(firstSyllable + secondSyllable + thirdSyllable)

                    tris.append(firstSyllable).append(secondSyllable).append(thirdSyllable)
                            .append("\n")
                }
            }
        }
        //return ones.toString() + twos.toString() + tris.toString()
        return allPossibleWords
    }

    /**list unused potential words which aren't too similar to existing words */
    internal fun listUnusedPotentialWords(wordsFromDictionary: Array<String>,
                                         allPossibleWords: MutableSet<String>): String {
        val b = StringBuilder()
        //String[] wordsFromDictionary = scrapeWordsFromDictionary(dictionaryFile);
        for (word in wordsFromDictionary) {
            allPossibleWords.remove(word)
            for (similarWord in similarWordsTo(word)) {
                allPossibleWords.remove(similarWord)
            }
        }
        o.println("unused words:")
        for (wurd in allPossibleWords) {
            //o.println(wurd);
            b.append(wurd).append("\n")
        }
        b.append("\ntotal: ").append(allPossibleWords.size)
        return b.toString()
    }

    internal fun lintTheDictionary(dict: Array<String>) {
        //todo: words with different harmonising vowels
        //todo: words with identical letters but their vowels swapped, or consonants swapped
        val dupCheck = TreeSet<String>()
        var complaints = 0
        for (word in dict) {
            //check for illegal letters
            val invalidLetters = word
                    .replace("[$vowelsString$consonantsString-]".toRegex(), "")
            if (invalidLetters.isNotEmpty()) {
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

            //make sure ending-taking words only have a '-' at the end
            if(word.contains("-")) {
                if(!word.endsWith("-")) {
                    o.println("word \"$word\" contains a hyphen in the wrong place")
                    complaints++
                }else {//word ends with -
                    //check that some of its verb-ending forms don't clash with another word
                    val formsWithDescriptions: Array<Array<String>> = arrayOf(
                            arrayOf(word.replace("-", "a"), "noun-form"),
                            arrayOf(word.replace("-", "u"), "noun-form"),
                            arrayOf(word.replace("-", "i"), "adjective-form")
                    )
                    for(s : Array<String> in formsWithDescriptions) {
                        if(dict.contains(s[0])) {
                            o.println("${s[1]} \"${s[0]}\" of word \"$word\" clashes with existing word")
                            complaints++
                        }
                        //check word-forms don't contain an illegal syllable
                        //check for any of the 4 illegal syllables
                        for (forb in forbiddenSyllables) {
                            if (s[0].contains(forb)) {
                                o.println("${s[1]} \"${s[0]}\" of word \"$word\" " +
                                        "contains illegal syllable \"$forb\"")
                                complaints++
                            }
                        }
                    }
                }
                if(word.count{it == '-'} > 1) {
                    o.println("word \"$word\" contains too many hyphens")
                    complaints++
                }
            }


            //check for syllable-final Ns
            if (!allowSyllableFinalN) {
                if (word.replace("n", "").length < word.length) {
                    //o.println("i:"+i);
                    for((j, c) in word.withIndex()) {
                        if(c == 'n') {
                            if(j == (word.length-1)) {
                                o.println("word \"$word\" contains a word-final N")
                                complaints++
                            }
                            else if (!("$vowelsString-").contains(word.elementAt(j + 1))) {
                                o.println("word \"$word\" contains an N before another consonant")
                                complaints++
                            }
                        }
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
            /*
            val similarWords = similarWordsTo(word)
            for (similarWord in similarWords) {
                //allPossibleWords.remove(similarWord)
                for (otherWord in dict) {
                    if (otherWord == similarWord) {
                        o.println("word \"$word\" is very similar to \"$otherWord\"")
                        complaints++
                    }
                }
            }*/
        }
        o.println("total complaints: $complaints")
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
                //similarWords.add(replaceCharAt(word, i, 'p'))
            }
            if (word[i] == 'k') {//replace k with t
                similarWords.add(replaceCharAt(word, i, 't'))
                //similarWords.add(replaceCharAt(word, i, 'p'))
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

    internal fun scrapeWordsFromDictionary(dictFile: File): Array<String> {
        //String wholeDict = fileToString(new File("dictionary.md"));
        val wholeDict = dictFile.readText()
        val byLine = wholeDict.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val words = mutableListOf<String>()
        for (i in 2 until byLine.size) {//start after the table heading
            if (byLine[i].count {it == '|'} != 5) {//if there aren't 5 pipes on the line, it's not a table row
                continue
            }
            //o.println("line: "+byLine[i]);
            val pat = Pattern.compile("([a-z]+-?) *\\|.*")
            val mat = pat.matcher(byLine[i])
            //o.println("group count: "+mat.groupCount());
            //o.println("group :"+mat.group());
            //o.println("matches: "+mat.matches());
            words.add(mat.replaceAll("$1"))
        }
        return words.toTypedArray()
    }
}
