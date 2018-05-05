//package default;

import java.util.*

import java.io.*
import java.util.regex.Pattern
import kotlin.collections.HashMap

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
    if(args.size >= 2) {
        val t = TokiNawaSounds()
        val command: String = args[0].toLowerCase()
        if ((command == "syllables" || command == "s")) {
            if("[1-3]".toRegex().matches(args[1])) {
                //print syllables
                val words: Set<String> = when(args[1].toInt()) {
                    1 -> t.listSingleSyllableWords()
                    2 -> t.listDoubleSyllableWords()
                    3 -> t.listTripleSyllableWords()
                    else -> emptySet()
                }

                for (s: String in words) {
                    o.println(s)
                }
                o.println("total: "+words.size)
            }else {
                e.println("was expecting a number argument 1-3.")
            }
        }
        else if(command == "similar" || command == "si") {
            for(s in t.similarWordsTo(args[1])) {
                o.println(s)
            }
        }
        else if(command == "anagram" || command == "a") {

            //val anagrams: Set<String> = t.anagram("", args[1].toList(), TreeSet<String>())
            val afterLastLetterNoHyphen = args[1].substring(1).replace("-", "")
            val anagrams: Set<String> = t.anagram(args[1][0].toString(), afterLastLetterNoHyphen, TreeSet<String>())
            for (anagram in anagrams) {
                if(anagram != args[1]) {
                    o.println("ANAGRAM:" + anagram)
                }
            }
        }
        else {
            //the other two commands require a second argument of a dictionary file,
            //so parse it as that
            //args[0].equals("lint")
            val dict = File(args[1])
            if(!dict.exists() || !dict.isFile || !dict.canRead() || dict.length() < 5) {
                e.println("invalid file specified.")
            }else {
                val dictionary = t.scrapeWordsFromDictionary(dict)
                if(command == "lint" || command == "l") {
                    t.lintTheDictionary(dictionary)
                }
                else if(command == "lexical-frequency" || command == "f") {
                    val firstLetterFreqs: MutableMap<Char, Int> = HashMap()
                    val letterFreqs: MutableMap<Char, Int> = HashMap()
                    for(word in dictionary) {
                        val c = word[0]
                        firstLetterFreqs[c] = firstLetterFreqs[c]?.plus(1) ?: 1

                        for(letter in word) {
                            letterFreqs[letter] = letterFreqs[letter]?.plus(1) ?: 1
                        }
                    }


                    o.println("first-letter frequencies:")
                    for((key, value) in firstLetterFreqs.toList().sortedBy {(_, v) -> v}.toMap()) {
                        o.println("$key: $value")
                    }



                    o.println("all-letter frequencies:")
                    for((key, value) in letterFreqs.toList().sortedBy {(_, v) -> v}.toMap()) {
                        o.println("$key: $value")
                    }

                }
                else if(command == "unused" || command == "u") {
                    /**list unused potential words which aren't too similar to existing words */
                    //populate the list of all potential words,
                    // then subtract all the dictionary words (and similar) from it
                    var totalSimilarWordsToDictionaryWords = 0
                    val allPossibleWords:MutableSet<String> = (
                            t.listSingleSyllableWords() +
                            t.listDoubleSyllableWords() +
                            t.listTripleSyllableWords()
                            ).toMutableSet()
                    val totalPossibleWords = allPossibleWords.size
                    o.println("total words: $totalPossibleWords")
                    //String[] wordsFromDictionary = scrapeWordsFromDictionary(dictionaryFile);
                    for (word in dictionary) {
                        allPossibleWords -= word
                        for (similarWord in t.similarWordsTo(word)) {
                            //o.println(similarWord)
                            allPossibleWords -= similarWord
                            totalSimilarWordsToDictionaryWords++
                        }
                    }
                    /*o.println("unused words:")
                    for (wurd in allPossibleWords) {
                        o.println(wurd);
                    }*/

                    o.println("total unused: ${allPossibleWords.size}")
                    o.println("dictionary words: ${dictionary.size}")
                    o.println("total similar words to dictionary words: " +
                            "$totalSimilarWordsToDictionaryWords")

                    if(args.size == 3){
                        o.println("word containing \"${args[2]}\":")
                        var matchingWords = 0
                        for(unusedWord in allPossibleWords.filter { it.contains(args[2]) }) {
                            o.println(unusedWord)
                            matchingWords++
                        }
                        o.println("matching words: $matchingWords")
                    }
                }
            }
        }
    }
    else {
        e.println("nope!")
    }
}

class TokiNawaSounds {

    private val consonants = "hjklmnpstw"
    private val vowels = "aiu"

    private val allowSyllableFinalN = true
    private val syllables = TreeSet<String>()
    private val wordInitialOnlySyllables = TreeSet<String>()
    private val wordInitialSyllables = TreeSet<String>()

    private val forbiddenSyllables = arrayOf("ji", "ti", "wu", "hu")

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
                if ((c + v) in forbiddenSyllables) {
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

    private fun containsForbiddenSyllable(word: String): Boolean {
        for (forbSyl in forbiddenSyllables) {
            if (forbSyl in word) {
                return true
            }
        }
        return false
    }

    /**list all possible single-syllable words (glue words)*/
    internal fun listSingleSyllableWords(): Set<String> {
        return wordInitialSyllables
    }

    /**list all possible double-syllable words*/
    internal fun listDoubleSyllableWords(): Set<String> {
        val twos = TreeSet<String>()

        //empty string represents missing initial consonant
        for(firstSyllable in wordInitialSyllables) {
            for(secondSyllable in syllables) {
                val twoSyls = firstSyllable+secondSyllable
                var shouldSkipThisOne = false
                /*val simis = similarWordsTo(twoSyls)
                for(similar in simis) {
                    if(twos.contains(similar)) {
                        shouldSkipThisOne = true
                        break
                    }
                }*/
                if(!shouldSkipThisOne) {
                    twos.add(twoSyls)
                }
            }
        }
        return twos
    }

    /**list all possible triple-syllable words */
    internal fun listTripleSyllableWords(): Set<String> {
        val tris = TreeSet<String>()

        for(firstSyllable in wordInitialSyllables) {
            for(secondSyllable in syllables) {
                for(thirdSyllable in syllables) {
                    val triSyls = firstSyllable+secondSyllable+thirdSyllable
                    var shouldSkipThisOne = false

                    /*val simis = similarWordsTo(triSyls)
                    for(similar in simis) {
                        if(tris.contains(similar)) {
                            shouldSkipThisOne = true
                            break
                        }
                    }*/
                    if(!shouldSkipThisOne) {
                        tris.add(triSyls)
                    }
                }
            }
        }
        return tris
    }

    internal fun lintTheDictionary(dict: Array<String>) {
        //todo: words with different harmonising vowels
        val dupCheck = TreeSet<String>()
        var complaints = 0
        for (word in dict) {
            //check for illegal letters
            val invalidLetters = word.filter { it !in vowels+consonants }
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

            //check for syllable-final Ns
            if (!allowSyllableFinalN) {
                if (word.replace("n", "").length < word.length) {
                    //o.println("i:"+i);
                    for(j in word.indices) {
                        if(word[j] == 'n') {
                            /*if(j == (word.length-1)) {
                                o.println("word \"$word\" contains a word-final N")
                                complaints++
                            }
                            else*/ if (j != (word.length-1)
                                    && consonants.contains(word[j + 1])) {
                                o.println("word \"$word\" contains an N before another consonant")
                                complaints++
                            }
                        }
                    }
                }
            }

            //check if this word contains another dictionary word
            for(otherWord in dict) {
                if(word.contains(otherWord) && otherWord.length > 2 && !word.equals(otherWord)) {
                    o.println("word \"$word\" contains other dictionary word \"$otherWord\"")
                    complaints++
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
                //allPossibleWords.remove(similarWord)
                for (otherWord in dict) {
                    if (otherWord == similarWord) {
                        o.println("word \"$word\" is very similar to \"$otherWord\"")
                        complaints++
                    }
                }
            }
        }
        o.println("total complaints: $complaints")
    }


    internal fun similarWordsTo(word: String): Array<String> {
        if (word.length == 1) {
            return arrayOf()
        }
        val similarWords = LinkedList<String>()
        for (i in 0 until word.length) {

            //replace u with the other vowels, and the other vowels for u
            if (word[i] == 'a') {//replace a with u
                similarWords.add(replaceCharAt(word, i, 'u'))
            }

            if (word[i] == 'u') {//replace u with a and i
                similarWords.add(replaceCharAt(word, i, 'a'))
                similarWords.add(replaceCharAt(word, i, 'i'))
            }

            if (word[i] == 'i') {//replace i with u
                similarWords.add(replaceCharAt(word, i, 'u'))
            }
/*
            if(consonants.contains(word[i])) {
                for (conso in consonants) {
                    if (conso != word[i]) {
                        val replaced = word.toCharArray()
                        replaced[i] = conso
                        similarWords.add(String(replaced))
                    }
                }
            }*/

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

            if (word[i] == 'm') {//replace m with n
                similarWords.add(replaceCharAt(word, i, 'n'))
            }
            if (word[i] == 't') {//replace t with k
                similarWords.add(replaceCharAt(word, i, 'k'))
                //similarWords.add(replaceCharAt(word, i, 'p'))
            }
            if (word[i] == 'k') {//replace k with t
                val wurd = replaceCharAt(word, i, 't')
                if(!containsForbiddenSyllable(wurd)){
                    similarWords.add(wurd)
                }
                //similarWords.add(replaceCharAt(word, i, 'p'))
            }
            if (word[i] == 'w') {//replace k with t
                similarWords.add(replaceCharAt(word, i, 'l'))
                //similarWords.add(replaceCharAt(word, i, 'p'))
            }
            if (word[i] == 'l') {//replace k with t
                val wurd = replaceCharAt(word, i, 'w')
                if(!containsForbiddenSyllable(wurd)){
                    similarWords.add(wurd)
                }
                //similarWords.add(replaceCharAt(word, i, 'p'))
            }


            //add phonotactically-valid anagrams beginning with the same letter
            val afterFirst = word.substring(1)
            val firstLetter = word[0]

            val anagrams = anagram(firstLetter.toString(), afterFirst, TreeSet<String>()).toMutableSet()
            anagrams.remove(word)//remove the word itself from the list of anagrams
            similarWords += anagrams
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
            val pipes = byLine[i].count {it == '|'}
            if (pipes != 4 && pipes != 1) {//if there aren't 4 pipes on the line, it's not a table row
                continue
            }
            //o.println("line: "+byLine[i]);
            val pat = Pattern.compile("([a-z]+)[ \t]*\\|.*")
            val mat = pat.matcher(byLine[i])
            //o.println("group count: "+mat.groupCount());
            //o.println("group :"+mat.group());
            //o.println("matches: "+mat.matches());
            words.add(mat.replaceAll("$1"))
        }
        return words.toTypedArray()
    }

    internal fun anagram(wordSoFar: String, lettersLeft: String,
                        accum: MutableSet<String> ): Set<String> {
        //o.println("letters left:"+lettersLeft.length)
        //o.println("word so far:"+wordSoFar)
        if (lettersLeft.isEmpty()) {
            //o.println("it's empty")
            //word is complete
            //o.println("anagram:"+wordSoFar)
            accum.add(wordSoFar)
            return accum
        }
        else {
            for(i in 0 until lettersLeft.length) {
                val thisChar = lettersLeft[i]
                if(wordSoFar[wordSoFar.length-1] in vowels && thisChar in consonants) {
                    //last letter was a vowel; next letter should be a consonant
                    //val minusTheLetter:MutableList<Char> = lettersLeft.toMutableList()
                    //minusTheLetter.removeAt(i)
                    val minusTheLetter = lettersLeft.removeRange(i, i+1)
                    anagram(wordSoFar + thisChar,
                            minusTheLetter, accum)
                }else if(wordSoFar[wordSoFar.length-1] in consonants && thisChar in vowels) {
                    //last letter was a consonant; next letter should be a vowel
                    //val minusTheLetter:MutableList<Char> = lettersLeft.toMutableList()
                    //minusTheLetter.removeAt(i)
                    val minusTheLetter = lettersLeft.removeRange(i, i+1)
                    anagram(wordSoFar + thisChar,
                            minusTheLetter, accum)
                }
            }
            return accum
        }
    }
}
