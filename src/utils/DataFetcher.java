package utils;

import java.util.ArrayList;
import java.util.HashSet;

import bean.MovieBean;
import bean.People;


public class DataFetcher {
	private static DBManager dbManger = DBManager.getInstance();

	private DataFetcher(){
	}
	
	public static ArrayList<String> getPeopleFavoriteType(String url, String peopleType){
		ArrayList<String> typeList = new ArrayList<String>();
		ArrayList<MovieBean> movieList = dbManger.getMovie(url, peopleType);
		for(MovieBean movieInfo:movieList){
			typeList.addAll(movieInfo.getType());
		}
		return new ArrayList<String>(new HashSet<String>(typeList));
	}
	
	public static ArrayList<People> getPeoplePartner(String url){
		ArrayList<People> partnerList = new ArrayList<People>();
		ArrayList<MovieBean> movieList = dbManger.getMovie(url);
		for(MovieBean movieInfo:movieList){
			partnerList.addAll(movieInfo.getActorList());
			partnerList.addAll(movieInfo.getDirectorList());
		}
		return new ArrayList<People>(new HashSet<People>(partnerList));
	}

}
