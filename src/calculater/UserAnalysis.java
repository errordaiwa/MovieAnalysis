package calculater;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import utils.DBManager;
import utils.FileManager;
import bean.MovieBean;
import bean.MovieEvaluate;
import bean.People;
import bean.UserBean;

public class UserAnalysis {
	private UserBean userBean;
	private DBManager dbManger;

	private HashMap<String, Integer> actorCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> directorCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> screenwriterCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> localCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> awardCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> tagCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> typeCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> upTimeCount = new HashMap<String, Integer>();
	private HashMap<String, Integer> scoreCount = new HashMap<String, Integer>();

	private int sum = 0;

	// todo! add score！！
	public static void main(String[] args) {
		UserAnalysis user = new UserAnalysis(
				"http://movie.douban.com/people/60648596/collect?start=");
		user.getCount();
		user.save(user.scoreCount);

	}

	public UserAnalysis(String url) {
		dbManger = DBManager.getInstance();
		userBean = dbManger.getUserBean(url);
	}

	private void getCount() {
		ArrayList<MovieEvaluate> movieEvaluateList = userBean
				.getMovieEvaluate();
		for (MovieEvaluate movieEvaluate : movieEvaluateList) {
			try {
				String movieUrl = movieEvaluate.getUrl();
				System.out.println(movieUrl);
				MovieBean movieBean = dbManger.getMovieInfo(movieUrl);
				if (movieBean == null)
					continue;
				setCount(changePeopleToString(movieBean.getActorList()),
						actorCount);
				setCount(changePeopleToString(movieBean.getDirectorList()),
						directorCount);
				setCount(movieBean.getScreenwriterList(), screenwriterCount);
				setCount(
						MovieSimilarity.changeLocalToList(movieBean.getLocal()),
						localCount);
				setCount(changeAwardToList(movieBean.isHasAward()), awardCount);
				setCount(movieBean.getTag(), tagCount);
				setCount(movieBean.getType(), typeCount);
				setCount(getYear(movieBean.getUpTime()), upTimeCount);
				setCount(changeScoreToList(movieBean.getScore()), scoreCount);
				sum++;
			} catch (Exception e) {
				continue;
			}

		}
		int a = 1;
	}

	private ArrayList<String> changeScoreToList(String score) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			double scoreDouble = Double.parseDouble(score);
			list.add(Double.toString(Math.floor(scoreDouble)));
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void setCount(ArrayList<String> list, HashMap<String, Integer> count) {
		if (list != null) {
			list = new ArrayList<String>(new HashSet<String>(list));
			for (String string : list) {
				if (count.containsKey(string.trim())) {
					count.put(string.trim(), count.get(string.trim()) + 1);
				} else {
					count.put(string.trim(), 1);
				}
			}
		}
	}

	private ArrayList<String> changePeopleToString(ArrayList<People> peopleList) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			for (People people : peopleList) {
				list.add(people.getUrl());
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<String> changeAwardToList(boolean hasAward) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			if (hasAward)
				list.add("hasAward");
			else
				list.add("noAward");
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private ArrayList<String> getYear(ArrayList<String> upTimeList) {
		try {
			ArrayList<String> list = new ArrayList<String>();
			for (String upTime : upTimeList) {
				try {
					list.add(upTime.substring(0, 4));
				} catch (Exception e) {
					continue;
				}
			}
			return list;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void save(HashMap<String, Integer> countMap) {
		FileManager fileManager = FileManager.getInstance();
		ArrayList<Count> countList = new ArrayList<Count>();
		Set<String> keySet = countMap.keySet();
		Iterator<String> keyIterator = keySet.iterator();
		while (keyIterator.hasNext()) {
			String name = (String) keyIterator.next();
			Count count = new Count(name, countMap.get(name));
			countList.add(count);
		}
		Collections.sort(countList);
		for (Count count : countList) {
			fileManager.save(String
					.format("%-20s\t%d", count.name, count.count));
		}
		fileManager.save(Integer.toString(sum));
	}

	class Count implements Comparable<Object> {
		String name;
		Integer count;

		public Count(String name, Integer count) {
			this.name = name;
			this.count = count;
		}

		@Override
		public int compareTo(Object o) {
			int flag = this.count - ((Count) o).count;
			if (flag < 0)
				return 1;
			else if (flag == 0)
				return 0;
			else
				return -1;
		}
	}

}
