package calculater;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import utils.DBManager;

import bean.MovieEvaluate;

public class DensityBasedSegmentation {
	private static final int MIN_LENGTH = 10;
	private static final double INCRE_MIN = 0.25;

	private ArrayList<Integer> cutPointList = new ArrayList<Integer>();
	private ArrayList<Double> incrementMaxList = new ArrayList<Double>();
	private ArrayList<String> wholeList;

	public void DBS(List<String> itemList) {
		int length = itemList.size();
		ArrayList<Double> incrementList = new ArrayList<Double>();
		for (int i = MIN_LENGTH; i <= length - MIN_LENGTH; i++) {
			incrementList.add(getCDI(itemList.subList(0, i - 2),
					itemList.subList(i, length - 1)));
		}
		double incrementMax = 0;
		int cutPoint = 0;
		if (incrementList.size() == 0) {
			return;
		} else {
			incrementMax = getMaxOfList(incrementList);
			cutPoint = incrementList.indexOf(incrementMax) + MIN_LENGTH - 1;
		}
		List<String> lSeg = itemList.subList(0, cutPoint - 1);
		List<String> rSeg = itemList.subList(cutPoint + 1, length - 1);

		if (incrementMax >= INCRE_MIN) {
			List<String> llSeg = getLeftSeg(itemList.get(0));
			List<String> rrSeg = getRightSeg(itemList.get(itemList.size() - 1));
			if (llSeg != null) {
				if (getCDI(llSeg, lSeg) < INCRE_MIN) {
					return;
				}
			}
			if (rrSeg != null) {
				if (getCDI(rrSeg, rSeg) < INCRE_MIN) {
					return;
				}
			}
			cutPointList.add(cutPoint);
			incrementMaxList.add(incrementMax);
			DBS(lSeg);
			DBS(rSeg);
		}
	}

	private double getMaxOfList(ArrayList<Double> list) {
		double max = 0;
		for (double item : list) {
			if (item > max) {
				max = item;
			}
		}
		return max;
	}

	private List<String> getLeftSeg(String startPoint) {
		int point = wholeList.indexOf(startPoint);
		int start = 0;
		int end = 0;
		for (int cutPoint : cutPointList) {
			if (cutPoint < point && cutPoint > end) {
				start = end;
				end = cutPoint;
			} else if (cutPoint < point && cutPoint > start) {
				start = cutPoint;
			}
		}
		return start < end ? wholeList.subList(start, end) : null;
	}

	private List<String> getRightSeg(String endPoint) {
		int point = wholeList.indexOf(endPoint);
		int start = wholeList.size() - 1;
		int end = wholeList.size() - 1;
		for (int cutPoint : cutPointList) {
			if (cutPoint > point && cutPoint < start) {
				end = start;
				start = cutPoint;
			} else if (cutPoint > point && cutPoint < end) {
				end = cutPoint;
			}
		}
		return start < end ? wholeList.subList(start, end) : null;
	}

	public double getCDI(List<String> itemSeg1, List<String> itemSeg2) {

		double T = 0;
		List<String> wholeList = new ArrayList<String>();
		wholeList.addAll(itemSeg1);
		wholeList.addAll(itemSeg2);
		T = (getDensity(itemSeg1) + getDensity(itemSeg2)) / 2.0
				- getDensity(wholeList);
		return T;
	}

	private double getDensity(List<String> movieList) {
		double density = 0;
		int len = movieList.size();
		int total = len * (len - 1) / 2;
		double edgeCount = getEdgeCount(movieList);
		density = edgeCount / total;
		return density;
	}
	
	private static final HashMap<String, Double> SIMILARITY_CACHE = new HashMap<String, Double>();

	private double getEdgeCount(List<String> A) {
		double numA = 0;
		for (int i = 0; i < A.size(); i++) {
			for (int j = i; j < A.size(); j++) {
				Double sim = SIMILARITY_CACHE.get(A.get(i) + A.get(j));
				if ( sim == null) {
					sim = new MovieSimilarity().calculateSimilaritySimple(
							A.get(i), A.get(j));
					SIMILARITY_CACHE.put(A.get(i) + A.get(j), sim);
				}
				if (sim >= 0.2) {
					numA++;
				}
			}
		}
		return numA;
	}

	public void setWholeList(ArrayList<String> wholeList) {
		this.wholeList = wholeList;
	}

	public ArrayList<Integer> getCutPointList() {
		return cutPointList;
	}

	public ArrayList<Double> getIncrementMaxList() {
		return incrementMaxList;
	}

	public static void main(String[] args) {
		ArrayList<MovieEvaluate> movieEvaluateList = DBManager
				.getInstance()
				.getUserBean(
						"http://movie.douban.com/people/60648596/collect?start=")
				.getMovieEvaluate();
		List<String> itemList = new ArrayList<String>();
		Collections.sort(movieEvaluateList, new Comparator<MovieEvaluate>() {

			@Override
			public int compare(MovieEvaluate o1, MovieEvaluate o2) {
				int date1 = Integer.parseInt(o1.getRatingTime().replaceAll("-", ""));
				int date2 = Integer.parseInt(o2.getRatingTime().replaceAll("-", ""));
				return date1 - date2;
			}
		});
		for (MovieEvaluate movieEvaluate : movieEvaluateList) {
			itemList.add(movieEvaluate.getUrl());
		}
		DensityBasedSegmentation dbs = new DensityBasedSegmentation();
		dbs.DBS(itemList);
		ArrayList<Integer> cutPointList = dbs.getCutPointList();
		ArrayList<Double> incrementMaxList = dbs.getIncrementMaxList();
	}

}
