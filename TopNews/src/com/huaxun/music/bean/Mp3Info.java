package com.huaxun.music.bean;

import java.io.Serializable;

/**
 * mp3实体类
 */
public class Mp3Info implements Serializable {
	private static final long serialVersionUID = 1L;
	private long id; // 歌曲ID 3
	private String title; // 歌曲名称 0
	private String album; // 专辑 7
	private long albumId;//专辑ID 6
	private String displayName; //显示名称 4
	private String artist; // 歌手名称 2
	private long duration; // 歌曲时长 1
	private long size; // 歌曲大小 8
	private String path; // 歌曲本地路径 5
	private String lrcTitle; // 歌词名称
	private String lrcSize; // 歌词大小 
	
	/** 下面几个属性用于网络搜索的音乐 */
	/** 小图URL */
	private String smallAlumUrl;
	/** 大图URL */
	private String bigAlumUrl; 
	/** 歌词地址 */
	private String lrcUrl;
	/** 歌曲地址 */
	private String musicUrl;	
	
	public Mp3Info() {
		super();
	}

	public Mp3Info(long id, String title, String album, long albumId,
			String displayName, String artist, long duration, long size,
			String path, String lrcTitle, String lrcSize) {
		super();
		this.id = id;
		this.title = title;
		this.album = album;
		this.albumId = albumId;
		this.displayName = displayName;
		this.artist = artist;
		this.duration = duration;
		this.size = size;
		this.path = path;
		this.lrcTitle = lrcTitle;
		this.lrcSize = lrcSize;
	}

	@Override
	public String toString() {
		return "Mp3Info [id=" + id + ", title=" + title + ", album=" + album
				+ ", albumId=" + albumId + ", displayName=" + displayName
				+ ", artist=" + artist + ", duration=" + duration + ", size="
				+ size + ", path=" + path + ", lrcTitle=" + lrcTitle
				+ ", lrcSize=" + lrcSize + ", lrcUrl=" + lrcUrl + ", musicUrl" + musicUrl + "]";
	}
	
    //注意，这里重写GetHashCode和equals方法
    public int hashCode()
    {
        return (int) id;
    } 
    
    public boolean equals(Object object) {
      if(object == null)
         return false;
    	
      if(object == this)
         return true;

      if(object instanceof Mp3Info) {
    	  Mp3Info mp3Info=(Mp3Info)object;
    	  return mp3Info.id == this.id;
       }
       return false;
    }

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	//有些歌曲的title会有类似  李荣浩 - 李白  这种写法，这时他们的artist是unknown，需要解析出来
	public String getTitle() {
		if (title.contains("-")) {
			return title.substring(title.lastIndexOf("-")+2);
		}
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	

	public String getArtist() {
		if (artist.equals("<unknown>")) {
			try {
				return title.substring(0,title.lastIndexOf("-")-1);
			} catch(Exception StringIndexOutOfBoundsException) {
				return artist; 
			}		
		}
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
	
	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getLrcTitle() {
		return lrcTitle;
	}

	public void setLrcTitle(String lrcTitle) {
		this.lrcTitle = lrcTitle;
	}

	public String getLrcSize() {
		return lrcSize;
	}

	public void setLrcSize(String lrcSize) {
		this.lrcSize = lrcSize;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getSmallAlumUrl() {
		return smallAlumUrl;
	}

	public void setSmallAlumUrl(String smallAlumUrl) {
		this.smallAlumUrl = smallAlumUrl;
	}

	public String getBigAlumUrl() {
		return bigAlumUrl;
	}

	public void setBigAlumUrl(String bigAlumUrl) {
		this.bigAlumUrl = bigAlumUrl;
	}

	public String getLrcUrl() {
		return lrcUrl;
	}

	public void setLrcUrl(String lrcUrl) {
		this.lrcUrl = lrcUrl;
	}

	public String getMusicUrl() {
		return musicUrl;
	}

	public void setMusicUrl(String musicUrl) {
		this.musicUrl = musicUrl;
	}
}