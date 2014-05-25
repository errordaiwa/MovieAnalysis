package calculater;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import main.MovieCrawler;

import utils.DBManager;
import utils.FileManager;
import bean.MovieBean;
import bean.People;

public class MovieSimilarity {
	private static DBManager dbManger = DBManager.getInstance();
	public static long YEAR = 365 * 24 * 3600;
	public double similarityOfDirectorTemp;
	public double similarityOfActorTemp;
	public ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors
			.newFixedThreadPool(100);
	private static HashMap<String, MovieBean> movieCache = new HashMap<String, MovieBean>();

	public static void main(String[] args) {
		MovieSimilarity m = new MovieSimilarity();
		System.out.println(m.calculateSimilarity(
				"http://movie.douban.com/subject/1759386/",
				"http://movie.douban.com/subject/2334904/"));
	}
	
	public void process() {
		
	}

	public double calculateSimilarity(String movieAUrl, String movieBUrl) {

		if (movieAUrl.equals(movieBUrl))
			return 1;
		MovieBean movieA = dbManger.getMovieInfo(movieAUrl);
		MovieBean movieB = dbManger.getMovieInfo(movieBUrl);
		if (movieA == null)
			movieA = crawlMovie(movieAUrl);
		if (movieB == null)
			movieB = crawlMovie(movieBUrl);
		if (movieA == null || movieB == null)
			return 0;
		double similarityOfUpTime = 0;
		try {
			similarityOfUpTime = getSimilarityOfYear(movieA.getUpTime().get(0)
					.split("\\(")[0], movieB.getUpTime().get(0).split("\\(")[0]);
		} catch (Exception e) {

		}
		double similarityOfType = getSimilarityOfStringList(movieA.getType(),
				movieB.getType());
		double similarityOfAward = getSimilarityOfAward(movieA.isHasAward(),
				movieB.isHasAward());
		double similarityOfLoc = getSimilarityOfStringList(
				changeLocalToList(movieA.getLocal()),
				changeLocalToList(movieA.getLocal()));
		double countOfDirector = getSimilarityOfPeople(movieA, movieB, false);
		double countOfActor = getSimilarityOfPeople(movieA, movieB, true);
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		double similarityOfDirector = similarityOfDirectorTemp
				/ countOfDirector;
		double similarityOfActor = similarityOfActorTemp / countOfActor;
		double similarityOfTag = getSimilarityOfStringList(movieA.getTag(),
				movieB.getTag());
		double similarityOfScore = getSimilarityOfScore(movieA, movieB);
		// double similarity = (similarityOfUpTime + 2*similarityOfType
		// + similarityOfAward +2*similarityOfLoc + 1.5*similarityOfDirector
		// + 1.5*similarityOfActor + similarityOfTag + 0.5*similarityOfScore) /
		// 10.5;// todo change权重
		double similarity = (similarityOfUpTime + 3 * similarityOfType
				+ similarityOfAward + 3 * similarityOfLoc
				+ similarityOfDirector + similarityOfActor + similarityOfTag + similarityOfScore) / 12;// todo
																										// change权重
		return similarity;
	}
	
	private static final MovieBean fakeMovie = new MovieBean();
	public double calculateSimilaritySimple(String movieAUrl, String movieBUrl) {
		if (movieAUrl.equals(movieBUrl))
			return 1.93;

		MovieBean movieA = movieCache.get(movieAUrl);
		if (movieA == null) {
			movieA = dbManger.getMovieInfo(movieAUrl);
			if (movieA == null) {
				movieCache.put(movieAUrl, fakeMovie);
				return 0;
			}
			movieCache.put(movieAUrl, movieA);
		} else if (movieA == fakeMovie) {
			return 0;
		}
		MovieBean movieB = movieCache.get(movieBUrl);
		if (movieB == null) {
			movieB = dbManger.getMovieInfo(movieBUrl);
			if (movieB == null) {
				movieCache.put(movieBUrl, fakeMovie);
				return 0;
			}
			movieCache.put(movieBUrl, movieB);
		} else if (movieB == fakeMovie) {
			return 0;
		}
//		if (movieA == null)
//			movieA = crawlMovie(movieAUrl);
//		if (movieB == null)
//			movieB = crawlMovie(movieBUrl);
		if (movieA == null || movieB == null)
			return 0;
		double similarityOfType = getSimilarityOfStringList(movieA.getType(),
				movieB.getType());
		double similarityOfTag = getSimilarityOfStringList(movieA.getTag(),
				movieB.getTag());
		double similarityOfDirector = getSimpleSimilarityOfPeople(movieA,
				movieB, false);
		double similarityOfActor = getSimpleSimilarityOfPeople(movieA, movieB,
				true);
		double similarity = similarityOfActor + 0.75 * similarityOfType + 0.17
				* similarityOfDirector + 0.1 * similarityOfTag;
		return similarity;
	}

	private static MovieBean crawlMovie(String movieAUrl) {
		MovieCrawler crawler = new MovieCrawler(movieAUrl);
		crawler.run();
		return dbManger.getMovieInfo(movieAUrl);
	}

	public static double getSimilarityOfYear(String birthdayStringA,
			String birthdayStringB) {
		try {
			long birthdayA = getBirthdayStamp(birthdayStringA);
			long birthdayB = getBirthdayStamp(birthdayStringB);
			double sub = Math.abs(birthdayA - birthdayB);
			double similarityOfBirthday = 5 / (sub / (YEAR * 1000));
			return (similarityOfBirthday >= 1) ? 1 : similarityOfBirthday;
		} catch (Exception e) {

			return 0;
		}
	}

	private static long getBirthdayStamp(String date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return dateFormat.parse(date).getTime();
		} catch (ParseException e) {
			return 0;
		}
	}

	public static double getSimilarityOfStringList(
			ArrayList<String> stringListA, ArrayList<String> stringListB) {

		try {
			@SuppressWarnings("unchecked")
			ArrayList<String> listUnion = (ArrayList<String>) stringListA
					.clone();
			@SuppressWarnings("unchecked")
			ArrayList<String> listIntersection = (ArrayList<String>) stringListB
					.clone();
			listUnion.removeAll(stringListB);
			listUnion.addAll(stringListB);
			listIntersection.retainAll(stringListA);

			return ((double) listIntersection.size())
					/ ((double) listUnion.size());
		} catch (Exception e) {
			return 0;
		}
	}

	public static double getSimilarityOfAward(boolean hasAwardA,
			boolean hasAwardB) {
		try {
			return (hasAwardA == hasAwardB) ? 1 : 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public static ArrayList<String> changeLocalToList(String local) {
		ArrayList<String> localList = new ArrayList<String>();
		String[] localStringList = local.split("/");
		for (int i = 0; i < localStringList.length; i++) {
			localList.add(localStringList[i]);
		}
		return localList;
	}

	private double getSimilarityOfPeople(MovieBean movieA, MovieBean movieB,
			boolean isActor) {
		try {
			ArrayList<String> peopleListA = getPeopleList(movieA, isActor);
			ArrayList<String> peopleListB = getPeopleList(movieB, isActor);
			return getSimilarityOfStringList(peopleListA, peopleListB);
		} catch (Exception e) {
			return 0;
		}
	}
	
	private double getSimpleSimilarityOfPeople(MovieBean movieA, MovieBean movieB,
			boolean isActor) {
		try {
			ArrayList<String> peopleListA = getPeopleList(movieA, isActor);
			ArrayList<String> peopleListB = getPeopleList(movieB, isActor);
			return (peopleListA.size() * peopleListB.size());
		} catch (Exception e) {
			return 0;
		}
	}

	private static ArrayList<String> getPeopleList(MovieBean movie,
			boolean isActor) {
		ArrayList<String> peopleList = new ArrayList<String>();
		for (People people : isActor ? movie.getActorList() : movie
				.getDirectorList()) {
			peopleList.add(people.getUrl());
		}
		return peopleList;
	}

	private static double getSimilarityOfScore(MovieBean movieA,
			MovieBean movieB) {
		try {
			double scoreA = Double.parseDouble(movieA.getScore());
			double scoreB = Double.parseDouble(movieB.getScore());
			return 1 / (1 + Math.abs(scoreA - scoreB));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(movieB.getMovieUrl());
			return 0;
		}
	}

	class PeopleSimilarityThread extends Thread {
		String peopleUrlA;
		String peopleUrlB;
		boolean isActor;

		PeopleSimilarityThread(String peopleUrlA, String peopleUrlB,
				boolean isActor) {
			this.peopleUrlA = peopleUrlA;
			this.peopleUrlB = peopleUrlB;
			this.isActor = isActor;
		}

		public void run() {
			if (isActor)
				similarityOfActorTemp += PeopleSimilarity.calculateSimilarity(
						peopleUrlA, peopleUrlB, DBManager.TYPE_ACTOR);
			else
				similarityOfDirectorTemp += PeopleSimilarity
						.calculateSimilarity(peopleUrlA, peopleUrlB,
								DBManager.TYPE_DIRECTOR);
		}
	}
}
