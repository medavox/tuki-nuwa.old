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
        if ((command == "syllables" || command == "s")) {
            if("[1-3]".toRegex().matches(args[1])) {
                //print syllables
                val syllableWords = TreeSet<String>()
                for(syls in 1..args[1].toInt()) {
                    syllableWords += t.listUpToTripleSyllableWords(syls)
                }
                for (s: String in syllableWords) {
                    o.println(s)
                }
                o.println("total: "+syllableWords.size)
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
            val dict: File = File(args[1])
            if(!dict.exists() || !dict.isFile || !dict.canRead() || dict.length() < 5) {
                e.println("invalid file specified.")
            }else {
                val dictContents = t.scrapeWordsFromDictionary(dict)
                if(command == "lint" || command == "l") {
                    t.lintTheDictionary(dictContents)
                }
                else if(command == "unused" || command == "u") {
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

    private val wordFinalConsonants = "klmps"
    private val wordMedialConsonants = wordFinalConsonants+"hjtw"
    private val wordInitialConsonants = wordMedialConsonants+"n"
    private val consonants = wordInitialConsonants
    private val vowels = "aiu"

    private val allowSyllableFinalN = false

    private val forbiddenSyllables = arrayOf("ji", "ti", "wu", "hu")

    /**list all possible words, up to triple-syllable words */
    internal fun listUpToTripleSyllableWords(syllableCount: Int = 3): Set<String> {
        if (syllableCount != 1 && syllableCount != 2 && syllableCount != 3) {
            throw IllegalArgumentException("syllable count must be 1, 2 or 3. " +
                    "Passed value: " + syllableCount)
        }

        val ones = TreeSet<String>()
        val twos = TreeSet<String>()
        val tris = TreeSet<String>()
        //val twos = StringBuilder()
        //val tris = StringBuilder()

        //empty string represents missing initial consonant
        val wordInitials: Array<String> = (consonants.map{it.toString()}+"").toTypedArray()
        //o.println(Arrays.toString(wordInitials))
        when (syllableCount) {
            1 -> {
                for(s: String in wordInitials) {
                    for(v: Char in vowels) {
                        val d = s+v
                        if(d !in forbiddenSyllables){
                            ones.add(d)
                        }
                    }
                }
            }
            2 -> {
                for(s: String in wordInitials) {
                    for(v: Char in vowels) {
                        for(d: Char in wordFinalConsonants) {
                            val twoSyls = s+v+d+"-"
                            var shouldSkipThisOne = false
                            for(forb in forbiddenSyllables) {
                                if(twoSyls.contains(forb)) {
                                    shouldSkipThisOne = true
                                    break
                                }
                            }
                            if(shouldSkipThisOne) {
                                continue
                            }
                            val simis = similarWordsTo(twoSyls)
                            //o.println("similar words: "+simis)
                            for(similar in simis) {
                                if(twos.contains(similar)) {
                                    shouldSkipThisOne = true
                                    break
                                }
                            }
                            if(shouldSkipThisOne) {
                                continue
                            }
                            twos.add(twoSyls)
                        }
                    }
                }
            }
            3 -> {
                for(s: String in wordInitials) {
                    for(v: Char in vowels) {
                        for(d: Char in wordMedialConsonants) {
                            for(v2: Char in vowels) {
                                for(d2: Char in wordFinalConsonants) {
                                    val triSyls = s+v+d+v2+d2+"-"
                                    var shouldSkipThisOne = false
                                    for(forb in forbiddenSyllables) {
                                        if(triSyls.contains(forb)) {
                                            shouldSkipThisOne = true
                                        }
                                    }
                                    if(shouldSkipThisOne) {
                                        continue
                                    }
                                    val simis = similarWordsTo(triSyls)
                                    for(similar in simis) {
                                        if(tris.contains(similar)) {
                                            shouldSkipThisOne = true
                                            break
                                        }
                                    }
                                    if(shouldSkipThisOne) {
                                        continue
                                    }
                                    tris.add(triSyls)
                                }
                            }
                        }
                    }
                }
            }
        }
        return ones+twos+tris
    }

    /**list unused potential words which aren't too similar to existing words */
    internal fun listUnusedPotentialWords(wordsFromDictionary: Array<String>,
                                         allPossibleWords: MutableSet<String>): String {
        val b = StringBuilder()
        var totalSimilarWordsToDictionaryWords = 0
        var dictionaryWords = 0
        //String[] wordsFromDictionary = scrapeWordsFromDictionary(dictionaryFile);
        for (word in wordsFromDictionary) {
            allPossibleWords.remove(word)
            dictionaryWords++
            for (similarWord in similarWordsTo(word)) {
                allPossibleWords.remove(similarWord)
                totalSimilarWordsToDictionaryWords++
            }
        }
        b.append("unused words:\n")
        for (wurd in allPossibleWords) {
            //o.println(wurd);
            b.append(wurd).append("\n")
        }
        b.append("\ntotal unused: ").append(allPossibleWords.size)
        b.append("\n dictionary words: ").append(dictionaryWords)
        b.append("\ntotal similar words to dictionary words: ")
                .append(totalSimilarWordsToDictionaryWords)
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
                    .replace("[$vowels$consonants-]".toRegex(), "")
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
                            else if (!("$vowels-").contains(word.elementAt(j + 1))) {
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


    internal fun similarWordsTo(word: String): Array<String> {
        if (word.length == 1) {
            return arrayOf()
        }
        val similarWords = LinkedList<String>()
        for (i in 0 until word.length) {
            val charAt = word[i].toString()

            //replace all vowels with all other vowels
            /*if (vowels.contains(charAt)) {//if this char is a vowel
                for (vowel in vowels) {
                    if (vowel != word[i]) {
                        val replaced = word.toCharArray()
                        replaced[i] = vowel
                        similarWords.add(String(replaced))
                    }
                }
            }*/
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

            if(word.endsWith('-')) {
                similarWords += similarWordsTo(word.replace("-", "a"))
                similarWords += similarWordsTo(word.replace("-", "i"))
                similarWords += similarWordsTo(word.replace("-", "u"))
            }

            //check for phonotactically-valid anagrams beginning with the same letter
            val afterFirst = word.substring(1).replace("-", "")
            val firstLetter = word[0]

            //add the anagrams of the word
            similarWords += anagram(firstLetter.toString(), afterFirst, TreeSet<String>())
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
                if(wordSoFar[wordSoFar.length-1] in vowels) {
                    //last letter was a vowel; next letter should be a consonant
                    if(lettersLeft.length == 1) {
                        if(thisChar in wordFinalConsonants) {
                            //val minusTheLetter:MutableList<Char> = lettersLeft.toMutableList()
                            //minusTheLetter.removeAt(i)
                            val minusTheLetter = lettersLeft.removeRange(i, i+1)
                            anagram(wordSoFar + thisChar,
                                    minusTheLetter, accum)
                        }
                        else {//else this path did not make a legal word, so exit
                            //e.println("anagram \"${wordSoFar+thisChar}\" " +
                             //       "is not a legal word; skipping...")
                            //return accum
                        }
                    }
                    else {//it's not the final consonant, so add any old one
                        if(thisChar in consonants) {
                            val minusTheLetter = lettersLeft.removeRange(i, i+1)
                            //val minusTheLetter:MutableList<Char> = lettersLeft.toMutableList()
                            //minusTheLetter.removeAt(i)
                            anagram(wordSoFar + thisChar,
                                    minusTheLetter, accum)
                        }
                    }
                }else if(wordSoFar[wordSoFar.length-1] in consonants
                    && thisChar in vowels) {
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
