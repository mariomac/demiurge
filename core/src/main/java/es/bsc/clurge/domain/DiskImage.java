package es.bsc.clurge.domain;

/**
 * Created by mmacias on 5/11/15.
 */
public class DiskImage {
	private String name;
	private String url;

	public DiskImage(String name, String url) {
		this.name = name;
		this.url = url;
	}
	public DiskImage(String id, String name, String status) {
		this.id = id;
		this.name = name;
		this.status = status;
	}


	public String getUrl() {
		return url;
	}

	private String id;

	private String status;


	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}
}
