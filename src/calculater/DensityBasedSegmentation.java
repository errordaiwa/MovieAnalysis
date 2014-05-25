package calculater;

import java.util.ArrayList;
import java.util.List;

public class DensityBasedSegmentation {
	private static final int MIN_LENGTH = 10;
	private static final double INCRE_MIN = 0.25;

	private ArrayList<Integer> cutPointList = new ArrayList<Integer>();
	private ArrayList<String> wholeList;

	public void DBS(List<String> itemList) {
		int length = itemList.size();
		ArrayList<Double> incrementList = new ArrayList<Double>();
		for (int i = MIN_LENGTH; i <= length - MIN_LENGTH; i++) {
			ArrayList<String> singleItem = new ArrayList<String>();
			singleItem.add(itemList.get(i - 1));
			incrementList.add(getCDI(itemList, singleItem));
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

	private double getCDI(List<String> itemSeg1, List<String> itemSeg2) {
		return 0;
	}

	public void setWholeList(ArrayList<String> wholeList) {
		this.wholeList = wholeList;
	}

	public ArrayList<Integer> getCutPointList() {
		return cutPointList;
	}

	public static void main(String[] args) {
	}

}
