/**
 Copyright (C) 2013-2014  Barcelona Supercomputing Center

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package es.bsc.clurge.vmm.generic;

import es.bsc.clurge.Clurge;
import es.bsc.clurge.domain.DiskImage;
import es.bsc.clurge.exception.CloudMiddlewareException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Ortiz Lopez (david.ortiz@bsc.es)
 */
public class GenericImageManager {

	private final Logger log = LogManager.getLogger(GenericImageManager.class);

    /**
     * Returns all the VM images in the system.
     *
     * @return the VM images
     */
    public List<DiskImage> getVmImages() {
        return Clurge.INSTANCE.getCloudMiddleware().getVmImages();
    }

    /**
     * Creates an image in the system.
     *
     * @param imageToUpload the image to be created/uploaded in the system
     * @return the ID of the image
     */
    public String createVmImage(DiskImage imageToUpload) throws CloudMiddlewareException {
		log.debug("CreateVMImage: " + imageToUpload.getName());
		return Clurge.INSTANCE.getCloudMiddleware().createVmImage(imageToUpload);
    }

    /**
     * Returns an image with the ID.
     *
     * @param imageId the ID of the image
     * @return the image
     */
    public DiskImage getVmImage(String imageId) {
        return Clurge.INSTANCE.getCloudMiddleware().getVmImage(imageId);
    }

    /**
     * Deletes a VM image.
     *
     * @param id the ID of the VM image
     */
    public void deleteVmImage(String id) {
		log.debug("DeleteVMImage: " + id);
		Clurge.INSTANCE.getCloudMiddleware().deleteVmImage(id);
    }

    /**
     * Returns the IDs of all the images in the system.
     *
     * @return the list of IDs
     */
    public List<String> getVmImagesIds() {
        List<String> vmImagesIds = new ArrayList<>();
        for (DiskImage imageDesc: Clurge.INSTANCE.getCloudMiddleware().getVmImages()) {
            vmImagesIds.add(imageDesc.getId());
        }
        return vmImagesIds;
    }
    
}
