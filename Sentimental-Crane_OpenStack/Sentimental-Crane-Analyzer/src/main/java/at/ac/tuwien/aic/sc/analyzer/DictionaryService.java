package at.ac.tuwien.aic.sc.analyzer;

import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// @Singleton
// @Startup
public class DictionaryService implements ConfigurableDictionaryServiceMXBean {
	private static final Logger logger = Logger.getLogger(DictionaryService.class.getName());
	private static DictionaryService instance;

	List<String> goodWords;
	List<String> badWords;
	List<String> stopWords;

	ObjectName name = null;

	public static DictionaryService getInstance() {
		if (instance == null) {
			synchronized (DictionaryService.class) {
				if (instance == null) {
					instance = new DictionaryService();
					instance.buildDefaultLists();
					instance.registerToJMX();
				}
			}
		}
		return instance;
	}

	// @PostConstruct
	public void buildDefaultLists() {
		goodWords = Arrays.asList(GOOD_WORDS);
		badWords = Arrays.asList(BAD_WORDS);
		stopWords = Arrays.asList(STOP_WORDS);
	}

	// @PostConstruct
	public void registerToJMX() {
		try {
			name = new ObjectName("at.ac.tuwien.aic.sc.analyzer:type=DictionaryService");
			ManagementFactory.getPlatformMBeanServer().registerMBean(this, name);
			logger.info("Successfully registered MBean DictionaryService");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error registering service", e);
		}
	}

	// @PreDestroy
	public void removeFromJMX() {
		try {
			ManagementFactory.getPlatformMBeanServer().unregisterMBean(name);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error unregistering service", e);
		}
	}

	public List<String> getGoodWords() {
		return Collections.unmodifiableList(goodWords);
	}

	public List<String> getBadWords() {
		return Collections.unmodifiableList(badWords);
	}

	public List<String> getStopWords() {
		return Collections.unmodifiableList(stopWords);
	}

	@Override
	public void addGoodWord(String goodWord) {
		addIfNotExists(goodWords, goodWord);
	}

	@Override
	public void addBadWord(String badWord) {
		addIfNotExists(badWords, badWord);
	}

	@Override
	public void addStopWord(String stopWord) {
		addIfNotExists(stopWords, stopWord);
	}

	private void addIfNotExists(List<String> list, String word) {
		if (!list.contains(word)) {
			list.add(word);
		}
	}

	@Override
	public void clearGoodWords() {
		goodWords.clear();
	}

	@Override
	public void clearBadWords() {
		badWords.clear();
	}

	@Override
	public void clearStopWords() {
		stopWords.clear();
	}


	private static final String[] GOOD_WORDS = (
			"Woo\n" +
			"quite amazing\n" +
			"thks\n" +
			"looking forward to\n" +
			"damn good\n" +
			"frickin ruled\n" +
			"frickin rules\n" +
			"Way to go\n" +
			"cute \n" +
			"comeback\n" +
			"not suck\n" +
			"prop\n" +
			"kinda impressed\n" +
			"props\n" +
			"come on\n" +
			"congratulation\n" +
			"gtd\n" +
			"proud\n" +
			"thanks\n" +
			"can help\n" +
			"thanks!\n" +
			"pumped\n" +
			"integrate\n" +
			"really like\n" +
			"loves it\n" +
			"yay\n" +
			"amazing\n" +
			"epic flail \n" +
			"flail\n" +
			"good luck\n" +
			"fail\n" +
			"life saver\n" +
			"piece of cake\n" +
			"good thing\n" +
			"hawt\n" +
			"hawtness\n" +
			"highly positive\n" +
			"my hero\n" +
			"yummy\n" +
			"awesome\n" +
			"congrats\n" +
			"would recommend\n" +
			"intellectual vigor\n" +
			"really neat\n" +
			"yay\n" +
			"ftw\n" +
			"I want\n" +
			"best looking\n" +
			"imrpessive\n" +
			"positive\n" +
			"thx\n" +
			"thanks\n" +
			"thank you\n" +
			"endorse\n" +
			"clearly superior\n" +
			"superior\n" +
			"really love\n" +
			"woot\n" +
			"w00t\n" +
			"super\n" +
			"wonderful\n" +
			"leaning towards\n" +
			"rally\n" +
			"incredible\n" +
			"the best\n" +
			"is the best\n" +
			"strong\n" +
			"would love\n" +
			"rally\n" +
			"very quickly\n" +
			"very cool\n" +
			"absolutely love\n" +
			"very exceptional\n" +
			"so proud\n" +
			"funny\n" +
			"recommend\n" +
			"so proud\n" +
			"so great\n" +
			"so cool\n" +
			"cool\n" +
			"wowsers\n" +
			"plus\n" +
			"liked it\n" +
			"make a difference\n" +
			"moves me\n" +
			"inspired\n" +
			"OK\n" +
			"love it\n" +
			"LOL\n" +
			":)\n" +
			";)\n" +
			":-)\n" +
			";-)\n" +
			":D\n" +
			";]\n" +
			":]\n" +
			":p\n" +
			";p\n" +
			"voting for \n" +
			"great\n" +
			"agreeable\n" +
			"amused\n" +
			"brave\n" +
			"calm\n" +
			"charming\n" +
			"cheerful\n" +
			"comfortable\n" +
			"cooperative\n" +
			"courageous\n" +
			"delightful\n" +
			"determined\n" +
			"eager\n" +
			"elated\n" +
			"enchanting\n" +
			"encouraging\n" +
			"energetic\n" +
			"enthusiastic\n" +
			"excited\n" +
			"exuberant\n" +
			"excellent\n" +
			"I like\n" +
			"fine\n" +
			"fair\n" +
			"faithful\n" +
			"fantastic\n" +
			"fine\n" +
			"friendly\n" +
			"fun \n" +
			"funny\n" +
			"gentle\n" +
			"glorious\n" +
			"good\n" +
			"pretty good\n" +
			"happy\n" +
			"healthy\n" +
			"helpful\n" +
			"high\n" +
			"agile\n" +
			"responsive\n" +
			"hilarious\n" +
			"jolly\n" +
			"joyous\n" +
			"kind\n" +
			"lively\n" +
			"lovely\n" +
			"lucky\n" +
			"nice\n" +
			"nicely\n" +
			"obedient\n" +
			"perfect\n" +
			"pleasant\n" +
			"proud\n" +
			"relieved\n" +
			"silly\n" +
			"smiling\n" +
			"splendid\n" +
			"successful\n" +
			"thankful\n" +
			"thoughtful\n" +
			"victorious\n" +
			"vivacious\n" +
			"witty\n" +
			"wonderful\n" +
			"zealous\n" +
			"zany\n" +
			"rocks\n" +
			"comeback\n" +
			"pleasantly surprised\n" +
			"pleasantly\n" +
			"surprised\n" +
			"love\n" +
			"glad\n" +
			"yum\n" +
			"interesting"
	).split("\n");

	public static final String[] BAD_WORDS = (
			"FTL\n" +
			"irritating \n" +
			"not that good\n" +
			"suck\n" +
			"lying\n" +
			"duplicity\n" +
			"angered\n" +
			"dumbfounding\n" +
			"dumbifying\n" +
			"not as good\n" +
			"not impressed\n" +
			"stomach it\n" +
			"pw\n" +
			"pwns\n" +
			"pwnd\n" +
			"pwning\n" +
			"in a bad way\n" +
			"horrifying\n" +
			"wrong\n" +
			"flailing\n" +
			"failing\n" +
			"fallen way behind\n" +
			"fallen behind\n" +
			"lose\n" +
			"fallen\n" +
			"self-deprecating\n" +
			"hunker down\n" +
			"duh\n" +
			"get killed by\n" +
			"got killed by\n" +
			"hated us\n" +
			"only works in safari\n" +
			"must have ie\n" +
			"fuming and frothing\n" +
			"heavy\n" +
			"buggy\n" +
			"unusable\n" +
			"nothing is\n" +
			"is great until\n" +
			"don't support\n" +
			"despise \n" +
			"pos\n" +
			"hindrance\n" +
			"sucks\n" +
			"problems\n" +
			"not working\n" +
			"fuming\n" +
			"annoying \n" +
			"frothing\n" +
			"poorly\n" +
			"headache\n" +
			"completely wrong\n" +
			"sad news\n" +
			"didn't last\n" +
			"lame\n" +
			"pet peeves\n" +
			"pet peeve\n" +
			"can't send\n" +
			"bullshit\n" +
			"fail\n" +
			"so terrible\n" +
			"negative\n" +
			"anooying\n" +
			"an issue\n" +
			"drop dead\n" +
			"trouble\n" +
			"brainwashed\n" +
			"smear\n" +
			"commie\n" +
			"communist\n" +
			"anti-women\n" +
			"WTF\n" +
			"anxiety\n" +
			"STING\n" +
			"nobody spoke\n" +
			"yell\n" +
			"Damn\n" +
			"aren't \n" +
			"anti\n" +
			"i hate\n" +
			"hate\n" +
			"dissapointing\n" +
			"doesn't recommend\n" +
			"the worst\n" +
			"worst\n" +
			"expensive\n" +
			"crap\n" +
			"socialist\n" +
			"won't\n" +
			"wont\n" +
			":(\n" +
			":-(\n" +
			"Thanks\n" +
			"smartass\n" +
			"don't like\n" +
			"too bad\n" +
			"frickin\n" +
			"snooty\n" +
			"knee jerk\n" +
			"jerk\n" +
			"reactionist\n" +
			"MUST DIE\n" +
			"no more\n" +
			"hypocrisy\n" +
			"ugly\n" +
			"too slow\n" +
			"not reliable\n" +
			"noise\n" +
			"crappy\n" +
			"horrible\n" +
			"bad quality\n" +
			"angry\n" +
			"annoyed\n" +
			"anxious\n" +
			"arrogant\n" +
			"ashamed\n" +
			"awful\n" +
			"bad\n" +
			"bewildered\n" +
			"blues\n" +
			"bored\n" +
			"clumsy\n" +
			"combative\n" +
			"condemned\n" +
			"confused\n" +
			"crazy\n" +
			"flipped-out\n" +
			"creepy\n" +
			"cruel\n" +
			"dangerous\n" +
			"defeated\n" +
			"defiant\n" +
			"depressed\n" +
			"disgusted\n" +
			"disturbed\n" +
			"dizzy\n" +
			"dull\n" +
			"embarrassed\n" +
			"envious\n" +
			"evil\n" +
			"fierce\n" +
			"foolish\n" +
			"frantic\n" +
			"frightened\n" +
			"grieving\n" +
			"grumpy\n" +
			"helpless\n" +
			"homeless\n" +
			"hungry\n" +
			"hurt\n" +
			"ill\n" +
			"itchy\n" +
			"jealous\n" +
			"jittery\n" +
			"lazy\n" +
			"lonely\n" +
			"mysterious\n" +
			"nasty\n" +
			"rape\n" +
			"naughty\n" +
			"nervous\n" +
			"nutty\n" +
			"obnoxious\n" +
			"outrageous\n" +
			"panicky\n" +
			"fucking up\n" +
			"repulsive\n" +
			"scary\n" +
			"selfish\n" +
			"sore\n" +
			"tense\n" +
			"terrible\n" +
			"testy\n" +
			"thoughtless\n" +
			"tired\n" +
			"troubled\n" +
			"upset\n" +
			"uptight\n" +
			"weary\n" +
			"wicked\n" +
			"worried\n" +
			"is a fool\n" +
			"painful\n" +
			"pain\n" +
			"gross"
	).split("\n");

	public static final String[] STOP_WORDS = (
			"a\n" +
			"about\n" +
			"above\n" +
			"after\n" +
			"again\n" +
			"against\n" +
			"all\n" +
			"am\n" +
			"an\n" +
			"and\n" +
			"any\n" +
			"are\n" +
			"as\n" +
			"at\n" +
			"be\n" +
			"because\n" +
			"been\n" +
			"before\n" +
			"being\n" +
			"below\n" +
			"between\n" +
			"both\n" +
			"but\n" +
			"by\n" +
			"could\n" +
			"did\n" +
			"do\n" +
			"does\n" +
			"doing\n" +
			"down\n" +
			"during\n" +
			"each\n" +
			"few\n" +
			"for\n" +
			"from\n" +
			"further\n" +
			"had\n" +
			"has\n" +
			"have\n" +
			"having\n" +
			"he\n" +
			"he'd\n" +
			"he'll\n" +
			"he's\n" +
			"her\n" +
			"here\n" +
			"here's\n" +
			"hers\n" +
			"herself\n" +
			"him\n" +
			"himself\n" +
			"his\n" +
			"how\n" +
			"how's\n" +
			"i\n" +
			"i'd\n" +
			"i'll\n" +
			"i'm\n" +
			"i've\n" +
			"if\n" +
			"in\n" +
			"into\n" +
			"is\n" +
			"it\n" +
			"it's\n" +
			"its\n" +
			"itself\n" +
			"let's\n" +
			"me\n" +
			"more\n" +
			"most\n" +
			"my\n" +
			"myself\n" +
			"no\n" +
			"nor\n" +
			"not\n" +
			"of\n" +
			"off\n" +
			"on\n" +
			"once\n" +
			"only\n" +
			"or\n" +
			"other\n" +
			"ought\n" +
			"our\n" +
			"ours\n" +
			"ourselves\n" +
			"out\n" +
			"over\n" +
			"own\n" +
			"same\n" +
			"she\n" +
			"she'd\n" +
			"she'll\n" +
			"she's\n" +
			"should\n" +
			"so\n" +
			"some\n" +
			"such\n" +
			"than\n" +
			"that\n" +
			"that's\n" +
			"the\n" +
			"their\n" +
			"theirs\n" +
			"them\n" +
			"themselves\n" +
			"then\n" +
			"there\n" +
			"there's\n" +
			"these\n" +
			"they\n" +
			"they'd\n" +
			"they'll\n" +
			"they're\n" +
			"they've\n" +
			"this\n" +
			"those\n" +
			"through\n" +
			"to\n" +
			"too\n" +
			"under\n" +
			"until\n" +
			"up\n" +
			"very\n" +
			"was\n" +
			"we\n" +
			"we'd\n" +
			"we'll\n" +
			"we're\n" +
			"we've\n" +
			"were\n" +
			"what\n" +
			"what's\n" +
			"when\n" +
			"when's\n" +
			"where\n" +
			"where's\n" +
			"which\n" +
			"while\n" +
			"who\n" +
			"who's\n" +
			"whom\n" +
			"why\n" +
			"why's\n" +
			"with\n" +
			"won't\n" +
			"would\n" +
			"you\n" +
			"you'd\n" +
			"you'll\n" +
			"you're\n" +
			"you've\n" +
			"your\n" +
			"yours\n" +
			"yourself\n" +
			"yourselves"
	).split("\n");
}
