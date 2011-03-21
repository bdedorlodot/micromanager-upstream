/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.micromanager.ometiff;

import java.io.IOException;
import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.common.services.ServiceFactory;
import loci.formats.FormatException;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.MetadataStore;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.services.OMEXMLService;
import mmcorej.TaggedImage;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.acquisition.TaggedImageStorageRam;
import org.micromanager.api.TaggedImageStorage;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.ReportingUtils;

/**
 *
 * @author arthur
 */
public class TaggedImageStorageOMETIFF implements TaggedImageStorage {
   public static String menuName_ = "OME TIFF";
   final private String location_;
   private boolean saved_ = true;
   ImageReader reader_ = null;
   private ImageWriter writer_ = null;
   private JSONObject summaryMetadata_ = null;
   private int planeIndex_;

   public TaggedImageStorageOMETIFF(String location, Boolean newData,
                                    JSONObject summaryMetadata) {
      summaryMetadata_ = summaryMetadata;
      if (!newData) {
         loadImages();
      }
      summaryMetadata_ = summaryMetadata;
      location_ = location;
   }

   @Override
   public TaggedImage getImage(int channel, int slice,
                               int frame, int position) {
      if (reader_ == null) {
         return null; // super.getImage(channel, slice, frame, position);
      } else {
         JSONObject tags = new JSONObject();
         reader_.setSeries(position);
         Object pix = reader_.getIndex(slice, channel, frame);
         TaggedImage image = new TaggedImage(pix, tags);
         return image;
      }
   }


   private void loadImages() {
      try {
         reader_ = new ImageReader();
         reader_.setId(location_);
         int nPositions = reader_.getSeriesCount();
         int nChannels = reader_.getSizeC();
         int nFrames = reader_.getSizeT();
         int nSlices = reader_.getSizeZ();
         int width = reader_.getSizeX();
         int height = reader_.getSizeY();
         
         MetadataStore metadata = reader_.getMetadataStore();

         for (int position = 0; position < nPositions; position++) {
            reader_.setSeries(position);

            byte[] pixels = reader_.openBytes(0);

            // this.putImage(null)
         }
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
      }

   }

   @Override
   public void finished() {
      if (writer_ != null)
         try {
            writer_.close();
         } catch (IOException ex) {
            ReportingUtils.logError(ex);
         }
   }

   public void putImage(TaggedImage taggedImage) {
      try {
         if (writer_ == null) {
            setupWriter();
         }

         writer_.setSeries(MDUtils.getPositionIndex(taggedImage.tags));
         writer_.saveBytes(planeIndex_, (byte[]) taggedImage.pix);
         ++planeIndex_;
        
      } catch (Exception ex) {
         ReportingUtils.showError(ex);
      }

   }

   private void setupWriter() throws Exception {
      writer_ = setupImageWriter(summaryMetadata_);
      planeIndex_ = 0;
   }

   private ome.xml.model.enums.DimensionOrder
           computeDimensionOrder(JSONObject summaryMetadata) {
      try {
         boolean slicesFirst = summaryMetadata_.getBoolean("SlicesFirst");
         return slicesFirst ? DimensionOrder.XYZCT : DimensionOrder.XYCZT;
      } catch (JSONException ex) {
         ReportingUtils.logError(ex);
         return DimensionOrder.XYCZT;
      }
   }

   private ImageWriter setupImageWriter(JSONObject summaryMetadata) throws FormatException, Exception, DependencyException, IOException, JSONException, ServiceException {
      saved_ = true;
      int nPositions = Math.max(1, summaryMetadata.getInt("Positions"));
      int nChannels = Math.max(1, summaryMetadata.getInt("Channels"));
      int nFrames = Math.max(1, summaryMetadata.getInt("Frames"));
      int nSlices = Math.max(1, summaryMetadata.getInt("Slices"));
      OMEXMLMetadata metadata = new ServiceFactory().getInstance(OMEXMLService.class).createOMEXMLMetadata();
      ImageWriter writer = new ImageWriter();
      for (int position = 0; position < nPositions; ++position) {
         String positionName = null;//MDUtils.getPositionName(super.getImage(0, 0, 0, position).tags);
         if (positionName == null) {
            positionName = "Single";
         }
         metadata.setImageID(positionName, position);
         metadata.setPixelsID("Pixels:" + position, position);
         metadata.setPixelsDimensionOrder(computeDimensionOrder(summaryMetadata), position);
         metadata.setPixelsBinDataBigEndian(true, position, 0);
         metadata.setPixelsSizeX(new PositiveInteger(MDUtils.getWidth(summaryMetadata)), position);
         metadata.setPixelsSizeY(new PositiveInteger(MDUtils.getHeight(summaryMetadata)), position);
         metadata.setPixelsSizeZ(new PositiveInteger(nSlices), position);
         metadata.setPixelsSizeC(new PositiveInteger(nChannels), position);
         metadata.setPixelsSizeT(new PositiveInteger(nFrames), position);
         metadata.setPixelsType(PixelType.UINT8, position);
         for (int channel = 0; channel < nChannels; ++channel) {
            metadata.setChannelID("Channel:" + position + ":" + channel, position, channel);
            metadata.setChannelSamplesPerPixel(new PositiveInteger(1), position, channel);
         }
      }
      writer.setMetadataRetrieve(metadata);
      writer.setId(location_ + ".ome.tiff");
      return writer;
   }
   
   public String getDiskLocation() {
      if (saved_) {
         return location_ + ".ome.tiff";
      } else {
         return null;
      }
   }

   public void setSummaryMetadata(JSONObject md) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public JSONObject getSummaryMetadata() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void setDisplayAndComments(JSONObject settings) {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public JSONObject getDisplayAndComments() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public void close() {
      throw new UnsupportedOperationException("Not supported yet.");
   }

   public int lastAcquiredFrame() {
      throw new UnsupportedOperationException("Not supported yet.");
   }
}