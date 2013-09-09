package calculater;

import java.util.ArrayList;
import java.util.HashSet;

import main.MovieCrawler;
import main.PeopleCrawler;

import bean.MovieBean;
import bean.People;
import bean.PeopleBean;
import utils.DBManager;

public class PeopleSimilarity {
	private static DBManager dbManger = DBManager.getInstance();

	// public static void main(String[] args) {
	// PeopleSimilarity.calculateSimilarity(
	// "http://movie.douban.com/celebrity/1028064/",
	// "http://movie.douban.com/celebrity/1033184/", "actor");
	// }

	private PeopleSimilarity() {
	}

	public static double calculateSimilarity(String peopleAUrl,
			String peopleBUrl, String peopleType) {
		if (peopleAUrl.equals(peopleBUrl))
			return 1;
		PeopleBean peopleA = dbManger.getPeople(peopleAUrl);
		PeopleBean peopleB = dbManger.getPeople(peopleBUrl);
//		if (peopleA == null)
//			peopleA = crawPeople(peopleAUrl);
//		if (peopleB == null)
//			peopleA = crawPeople(peopleBUrl);
		if (peopleA == null || peopleB == null)
			return 0;
		double similarityOfBirthday = MovieSimilarity.getSimilarityOfYear(
				peopleA.getBirthday(), peopleB.getBirthday());
		double similarityOfType = MovieSimilarity.getSimilarityOfStringList(
				getPeopleFavoriteType(peopleAUrl, peopleType),
				getPeopleFavoriteType(peopleBUrl, peopleType));
		double similarityOfPartner = getSimilarityOfPartner(peopleAUrl,
				peopleBUrl);
		double similarityOfAward = MovieSimilarity.getSimilarityOfAward(
				peopleA.isHasAward(), peopleB.isHasAward());
		double similarityOfLoc = getSimilarityOfLoc(peopleA, peopleB);
		double similarityOfSex = getSimilarityOfSex(peopleA, peopleB);
		double similarity = (similarityOfBirthday + similarityOfType
				+ similarityOfPartner + similarityOfAward + similarityOfLoc + similarityOfSex) / 6;
		return similarity * 0.8;
	}

	private static PeopleBean crawPeople(String peopleAUrl) {
		PeopleCrawler crawler = new PeopleCrawler(peopleAUrl);
		crawler.run();
		return dbManger.getPeople(peopleAUrl);
	}

	private static double getSimilarityOfPartner(String peopleAUrl,
			String peopleBUrl) {
		try {
			ArrayList<People> partnerListA = getPeoplePartner(peopleAUrl);
			ArrayList<People> partnerListB = getPeoplePartner(peopleBUrl);
			@SuppressWarnings("unchecked")
			ArrayList<People> partnerListUnion = (ArrayList<People>) partnerListA
					.clone();
			@SuppressWarnings("unchecked")
			ArrayList<People> partnerListIntersection = (ArrayList<People>) partnerListB
					.clone();
			partnerListUnion.removeAll(partnerListB);
			partnerListUnion.addAll(partnerListB);
			partnerListIntersection.retainAll(partnerListA);

			return ((double) partnerListIntersection.size())
					/ ((double) partnerListUnion.size());
		} catch (Exception e) {
			return 0;
		}
	}

	private static double getSimilarityOfLoc(PeopleBean peopleA,
			PeopleBean peopleB) {
		try {
			return (peopleA.getBirthplace().split(",")[0].equals(peopleB
					.getBirthplace().split(",")[0])) ? 1 : 0;
		} catch (Exception e) {
			return 0;
		}
	}

	private static double getSimilarityOfSex(PeopleBean peopleA,
			PeopleBean peopleB) {
		try {
			return (peopleA.getSex().equals(peopleB.getSex())) ? 1 : 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public static ArrayList<String> getPeopleFavoriteType(String url,
			String peopleType) {
		ArrayList<String> typeList = new ArrayList<String>();
		ArrayList<MovieBean> movieList = dbManger.getMovie(url, peopleType);
		for (MovieBean movieInfo : movieList) {
			if (movieInfo.getType() != null)
				typeList.addAll(movieInfo.getType());
		}
		return new ArrayList<String>(new HashSet<String>(typeList));
	}

	public static ArrayList<People> getPeoplePartner(String url) {
		ArrayList<People> partnerList = new ArrayList<People>();
		ArrayList<MovieBean> movieList = dbManger.getMovie(url);
		for (MovieBean movieInfo : movieList) {
			if (movieInfo.getActorList() != null)
				partnerList.addAll(movieInfo.getActorList());
			if (movieInfo.getDirectorList() != null)
				partnerList.addAll(movieInfo.getDirectorList());
		}
		return new ArrayList<People>(new HashSet<People>(partnerList));
	}
}
