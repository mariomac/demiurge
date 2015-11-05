package es.bsc.clurge;

import es.bsc.clurge.domain.DiskImage;
import es.bsc.clurge.exception.CloudMiddlewareException;

import java.util.List;

/**
 * Created by mmacias on 5/11/15.
 */
public interface ImageManager {
	/**
	 * Returns all the VM images in the system.
	 *
	 * @return the VM images
	 */
	List<DiskImage> getVmImages();

	/**
	 * Creates an image in the system.
	 *
	 * @param imageToUpload the image to be created/uploaded in the system
	 * @return the ID of the image
	 */
	String createVmImage(DiskImage imageToUpload) throws CloudMiddlewareException;

	/**
	 * Returns an image with the ID.
	 *
	 * @param imageId the ID of the image
	 * @return the image
	 */
	DiskImage getVmImage(String imageId);

	/**
	 * Deletes a VM image.
	 *
	 * @param id the ID of the VM image
	 */
	void deleteVmImage(String id);

	/**
	 * Returns the IDs of all the images in the system.
	 *
	 * @return the list of IDs
	 */
	List<String> getVmImagesIds();

}
