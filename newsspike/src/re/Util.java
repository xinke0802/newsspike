package re;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javatools.datatypes.HashCount;
import javatools.filehandlers.DR;

import com.google.common.collect.HashMultimap;

public class Util {
	public static String pairAB(String a, String b) {
		if (a.compareTo(b) > 0) {
			return a + "::" + b;
		} else {
			return b + "::" + a;
		}
	}

	public static String getDayInStringOfDate(Date date) {
		SimpleDateFormat dateformatYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
		String today = dateformatYYYYMMDD.format(date);
		return today;
	}

	public static void getRelationPairsFromClustering(String input,
			Set<String> pairs, Set<String> diffheadPairs) {
		DR dr = new DR(input);
		String[] l;
		List<String[]> lines = new ArrayList<String[]>();
		while ((l = dr.read()) != null) {
			if (l[0].equals("###")) {
				List<String> rels = new ArrayList<String>();
				List<String> heads = new ArrayList<String>();
				for (String[] ll : lines) {
					if (Double.parseDouble(ll[0]) > 0.9) {
						String r = Tuple.getRidOfOlliePartOfRelation(ll[1]);
						rels.add(r);
						heads.add(ll[2]);
					}
				}
				for (int i = 0; i < rels.size(); i++) {
					for (int j = i + 1; j < rels.size(); j++) {
						String a = rels.get(i);
						String b = rels.get(j);
						if (!a.equals(b)) {
							String p = Util.pairAB(a, b);
							pairs.add(p);
							//						if (a.compareTo(b) > 0) {
							//							a = rels.get(j);
							//							b = rels.get(i);
							//						}
							//						pairs.add(a + "::" + b);
							if (!heads.get(i).equals(heads.get(j))) {
								diffheadPairs.add(p);
							}
						}
					}
				}
				lines = new ArrayList<String[]>();
			} else {
				lines.add(l);
			}
		}
		dr.close();
	}

	public static HashSet<String> getPositiveRelationsFromClustering(
			String input) {
		HashSet<String> correct = new HashSet<String>();
		{
			DR dr = new DR(input);
			String[] l;
			List<String[]> lines = new ArrayList<String[]>();
			int NewsSpikeId = -1;
			while ((l = dr.read()) != null) {
				if (l[0].equals("###")) {
					List<String> rels = new ArrayList<String>();
					for (String[] ll : lines) {
						if (Double.parseDouble(ll[0]) > 0.9) {
							correct.add(NewsSpikeId + "\t" + ll[1]);
						}
					}
					lines = new ArrayList<String[]>();
					if (l.length > 1) {
						NewsSpikeId = Integer.parseInt(l[2]);
					}
				} else {
					lines.add(l);
				}
			}
			dr.close();
		}
		return correct;
	}

	public static HashMap<Integer, List<String[]>> loadClusterOutput(String file) {
		HashMap<Integer, List<String[]>> ret = new HashMap<Integer, List<String[]>>();
		DR dr = new DR(file);
		String[] l;
		List<String[]> lines = new ArrayList<String[]>();
		int newsspikeid = 0;
		while ((l = dr.read()) != null) {
			if (l[0].equals("###")) {
				ret.put(newsspikeid, lines);
				lines = new ArrayList<String[]>();
				if (l.length > 1)
					newsspikeid = Integer.parseInt(l[2]);
			} else {
				lines.add(l);
			}
		}
		return ret;
	}

	public static HashMultimap<Integer, String> loadClusterOutputInStrHardonly(
			String file) {
		HashMultimap<Integer, String> ret = HashMultimap.create();
		DR dr = new DR(file);
		String[] l;
		List<String[]> lines = new ArrayList<String[]>();
		int newsspikeid = -1;
		while ((l = dr.read()) != null) {
			if (l[0].equals("###")) {
				if (newsspikeid >= 0 && lines.size() > 0) {
					HashSet<String> temp = new HashSet<String>();
					for (String[] a : lines) {
						String r = a[2];
						String key = newsspikeid
								+ "\t" + r;
						if (Double.parseDouble(a[0]) > 0.99
								&& !ret.containsEntry(newsspikeid, key)) {
							temp.add(key);
						}
					}
					if (temp.size() > 1) {
						for (String k : temp)
							ret.put(newsspikeid, k);
					}
				}
				lines = new ArrayList<String[]>();
				if (l.length > 1)
					newsspikeid = Integer.parseInt(l[2]);
			} else {
				lines.add(l);
			}
		}
		return ret;
	}

	public static HashMultimap<Integer, String> loadClusterOutputInStrHardonly0702(
			String file) {
		HashMultimap<Integer, String> ret = HashMultimap.create();
		DR dr = new DR(file);
		String[] l;
		List<String[]> lines = new ArrayList<String[]>();
		int newsspikeid = -1;
		while ((l = dr.read()) != null) {
			if (l[0].equals("###")) {
				//				ret.put(newsspikeid, lines);
				if (newsspikeid >= 0 && lines.size() > 0) {
					//					HashCount<String> hc = countHead(lines);
					String majorityHead = majorityHead(lines);
					for (String[] a : lines) {
						String r = Tuple.getRidOfOlliePartOfRelation(a[1]);
						//						int c = headcount.see(a[2]);
						if (Double.parseDouble(a[0]) > 0.99
								&& !a[2].equals(majorityHead)
								//								&& hc.see(a[2]) == 1
								&& !ret.containsEntry(newsspikeid, newsspikeid
										+ "\t" + r)) {
							ret.put(newsspikeid, newsspikeid + "\t" + r);
						}
					}
				}
				lines = new ArrayList<String[]>();
				if (l.length > 1)
					newsspikeid = Integer.parseInt(l[2]);
			} else {
				lines.add(l);
			}
		}
		return ret;
	}

	public static HashCount<String> countHead(List<String[]> lines) {
		//		HashSet<String> appeared = new HashSet<String>();
		HashCount<String> headcount = new HashCount<String>();
		for (String[] a : lines) {
			String r = Tuple.getRidOfOlliePartOfRelation(a[1]);
			//			if (!appeared.contains(r)) {
			headcount.add(a[2]);
			//				appeared.add(r);
			//			}
		}
		return headcount;
	}

	public static String majorityHead(List<String[]> lines) {
		HashSet<String> appeared = new HashSet<String>();
		HashCount<String> headcount = new HashCount<String>();
		for (String[] a : lines) {
			String r = Tuple.getRidOfOlliePartOfRelation(a[1]);
			if (!appeared.contains(r)) {
				headcount.add(a[2]);
				appeared.add(r);
			}
		}
		int max = 0;
		String ret = null;
		for (Entry<String, Integer> e : headcount.entries()) {
			int c = e.getValue();
			if (c >= 2 && c > max) {
				ret = e.getKey();
			}
		}
		if (ret == null) {
			ret = "NO_MAJORITY";
		}
		return ret;
	}

	public static List<String[]> evalsingle(
			HashMap<Integer, List<String[]>> gold,
			HashMap<Integer, List<String[]>> answer,
			int[] ret) {
		List<String[]> debug = new ArrayList<String[]>();
		for (Entry<Integer, List<String[]>> e : gold.entrySet()) {
			int modelId = e.getKey();
			int[] a = new int[6];
			List<String[]> glines = gold.get(modelId);
			if (answer.containsKey(modelId)) {
				List<String[]> alines = answer.get(modelId);
				evalsingleHelp(glines, alines, a, debug, modelId);
			} else {
				evalsingleHelpNoRetAnswer(glines, a, debug, modelId);
			}
			for (int i = 0; i < a.length; i++) {
				ret[i] += a[i];
			}
		}
		//		for (Entry<Integer, List<String[]>> e : answer.entrySet()) {
		//			int modelId = e.getKey();
		//			if (gold.containsKey(modelId)) {
		//				List<String[]> glines = gold.get(modelId);
		//				List<String[]> alines = answer.get(modelId);
		//				int[] a = new int[6];
		//				evalsingleHelp(glines, alines, a, debug);
		//				for (int i = 0; i < a.length; i++) {
		//					ret[i] += a[i];
		//				}
		//			}
		//		}
		Collections.sort(debug, new Comparator<String[]>() {
			@Override
			public int compare(String[] arg0, String[] arg1) {
				return arg0[0].compareTo(arg1[0]);
			}
		});
		return debug;
	}

	private static void evalsingleHelp_0702(List<String[]> glines,
			List<String[]> alines, int[] ret, List<String[]> debug,
			int modelId) {
		//set ret[0]
		String majorityHead = majorityHead(glines);
		{
			Set<String> golds = new HashSet<String>();
			Set<String> answers = new HashSet<String>();
			for (String[] l : glines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					golds.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			for (String[] l : alines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					answers.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			//			if (answers.size() > 1) {

			for (String s : answers) {
				if (golds.contains(s)) {
					ret[0]++;
				} else {
					debug.add(new String[] { "P", s, modelId + "" });
				}
				ret[1]++;
			}

			for (String s : golds) {
				if (!answers.contains(s)) {
					debug.add(new String[] { "R", s, modelId + "" });
				}
			}
			//			ret[1] = answers.size();
			ret[2] = golds.size();
		}
		{
			Set<String> golds = new HashSet<String>();
			Set<String> answers = new HashSet<String>();
			HashCount<String> headcount = new HashCount<String>();
			for (String[] l : glines) {
				headcount.add(l[2]);
			}
			for (String[] l : glines) {
				int c = headcount.see(l[2]);
				if (Double.parseDouble(l[0]) > 0.9
						&& !majorityHead.equals(l[2])
				//						&& c <= 1
				) {
					golds.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			for (String[] l : alines) {
				if (Double.parseDouble(l[0]) > 0.9
						&& !majorityHead.equals(l[2])
				//						headcount.see(l[2]) <= 1
				) {
					answers.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			for (String s : answers) {
				if (golds.contains(s)) {
					ret[3]++;
				}
			}
			ret[4] = answers.size();
			ret[5] = golds.size();
		}
	}

	private static void evalsingleHelp(List<String[]> glines,
			List<String[]> alines, int[] ret, List<String[]> debug,
			int modelId) {
		//set ret[0]
		//		String majorityHead = majorityHead(glines);
		{
			Set<String> golds = new HashSet<String>();
			Set<String> answers = new HashSet<String>();
			for (String[] l : glines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					golds.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			for (String[] l : alines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					answers.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			//			if (answers.size() > 1) {

			for (String s : answers) {
				if (golds.contains(s)) {
					ret[0]++;
				} else {
					debug.add(new String[] { "P", s, modelId + "" });
				}
				ret[1]++;
			}

			for (String s : golds) {
				if (!answers.contains(s)) {
					debug.add(new String[] { "R", s, modelId + "" });
				}
			}
			//			ret[1] = answers.size();
			ret[2] = golds.size();
		}
		{
			Set<String> golds = new HashSet<String>();
			Set<String> answers = new HashSet<String>();
			HashCount<String> headcount = new HashCount<String>();
			for (String[] l : glines) {
				headcount.add(l[2]);
			}
			for (String[] l : glines) {
				int c = headcount.see(l[2]);
				if (Double.parseDouble(l[0]) > 0.9) {
					golds.add(l[2]);
				}
			}
			for (String[] l : alines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					answers.add(l[2]);
				}
			}
			if (answers.size() > 1 && golds.size() > 1) {
				for (String s : answers) {
					if (golds.contains(s)) {
						ret[3]++;
					}
				}
			}
			if (answers.size() > 1) {
				ret[4] = answers.size();
			}
			if (golds.size() > 1) {
				ret[5] = golds.size();
			}
		}
	}

	

	private static void evalsingleHelpNoRetAnswer(List<String[]> glines,
			int[] ret, List<String[]> debug, int modelId) {
		//set ret[0]
		{
			Set<String> golds = new HashSet<String>();
			for (String[] l : glines) {
				if (Double.parseDouble(l[0]) > 0.9) {
					golds.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}

			for (String s : golds) {
				debug.add(new String[] { "R", s, modelId + "" });
			}
			ret[2] = golds.size();
		}
		{
			Set<String> golds = new HashSet<String>();
			HashCount<String> headcount = new HashCount<String>();
			for (String[] l : glines) {
				headcount.add(l[2]);
			}
			for (String[] l : glines) {
				int c = headcount.see(l[2]);
				if (Double.parseDouble(l[0]) > 0.9 && c <= 1) {
					golds.add(Tuple.getRidOfOlliePartOfRelation(l[1]));
				}
			}
			ret[5] = golds.size();
		}
	}

}
