package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import calculater.MovieSimilarity;

import bean.MovieEvaluate;
import bean.UserBean;

/**
 * 
 *  @author xingsu
 *
 */
public class FileManager {
	
	private static FileManager fileManager = new FileManager();
	private String filePath = "D:/countList.txt";
	private final String filePath2 = "D:/movieUrlList.txt";
	
//	public static void main(String[] args){
//		UserBean u = DBManager.getInstance().getUserBean("http://movie.douban.com/people/xuancanna/collect?start=");
//		FileManager.getInstance().saveUserToFile(u);
//	}
	
	public static void main(String[] args){
		FileManager.getInstance().readUrlFromFile("http://movie.douban.com/subject/2043155/");
	}
	
	private FileManager(){

		
	}
	
	public static FileManager getInstance(){
		return fileManager;
	}
	
	public void save(String string) {
		if(string == null)
			return;
		File file =  new File(filePath);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
        String filein = string + "\r\n";
        String temp = "";

        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        PrintWriter pw = null;
        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            StringBuffer buf = new StringBuffer();

            while ((temp = br.readLine()) != null) {
                buf = buf.append(temp);
                buf = buf.append(System.getProperty("line.separator"));
            }
            buf.append(filein);

            fos = new FileOutputStream(file);
            pw = new PrintWriter(fos);
            pw.write(buf.toString().toCharArray());
            pw.flush();
        } catch (IOException e) {
        } finally {
                try {
					pw.close();
					fos.close();
					br.close();
					isr.close();
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
        }
	}
	
	public void readUrlFromFile(String movieAUrl){
		File file =  new File(filePath2);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String temp = "";
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
		try {
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
			StringBuffer buf = new StringBuffer();
			
			while ((temp = br.readLine()) != null) {
//				UrlBean urlBean = new UrlBean();
//				urlBean.setUrl(temp);
//				urlBean.setType(Const.typeMovie);
//				urlBean.setNeedToSavePeople(true);
//				urlBean.setNeedToSaveUser(false);
//				DBManager.getInstance().saveToDB(urlBean);
				System.out.println(temp);
				MovieSimilarity m = new MovieSimilarity();
				double similarity = m.calculateSimilarity(movieAUrl, temp);
				
				try {
					fileManager.save(String.format("%s\t%f\t%s", temp, similarity, DBManager.getInstance().getMovieInfo(temp).getMovieName()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveUserToFile(UserBean userBean){
		filePath = "D:/user.txt";
		save(userBean.getUserName());
		save(userBean.getUserUrl());
		for(MovieEvaluate movieEvaluate : userBean.getMovieEvaluate()){
			save(movieEvaluate.getUrl());
			save(movieEvaluate.getRating());
			save(movieEvaluate.getRatingTime());
		}
		
	}

}
