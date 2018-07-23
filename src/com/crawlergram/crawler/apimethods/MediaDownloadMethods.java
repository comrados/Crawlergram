/*
 * Title: MediaDownloadMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 * 2018
 */

package com.crawlergram.crawler.apimethods;

import com.crawlergram.crawler.output.FileMethods;
import org.telegram.api.document.TLAbsDocument;
import org.telegram.api.document.TLDocument;
import org.telegram.api.document.attribute.*;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.file.location.TLAbsFileLocation;
import org.telegram.api.file.location.TLFileLocation;
import org.telegram.api.input.filelocation.TLAbsInputFileLocation;
import org.telegram.api.input.filelocation.TLInputDocumentFileLocation;
import org.telegram.api.input.filelocation.TLInputFileLocation;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.message.TLMessage;
import org.telegram.api.message.media.TLAbsMessageMedia;
import org.telegram.api.message.media.TLMessageMediaDocument;
import org.telegram.api.message.media.TLMessageMediaPhoto;
import org.telegram.api.photo.TLAbsPhoto;
import org.telegram.api.photo.TLPhoto;
import org.telegram.api.photo.size.TLAbsPhotoSize;
import org.telegram.api.photo.size.TLPhotoSize;
import org.telegram.api.upload.file.TLAbsFile;
import org.telegram.api.upload.file.TLFile;
import org.telegram.tl.TLVector;
import com.crawlergram.db.DBStorage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MediaDownloadMethods {

    /**
     * Gets the file name and bytes, saves to HDD, in case of success returns the resulting filepath
     * @param api api
     * @param absMessage message
     * @param maxSize max size of medial to be saved
     * @param path path on the HDD
     */
    public static String messageDownloadMediaToHDD(TelegramApi api, TLAbsMessage absMessage, int maxSize, String path) {
        String out = null;
        if (absMessage instanceof TLMessage) {
            if (((TLMessage) absMessage).hasMedia()) {
                byte[] bytes = messageGetMediaBytes(api, (TLMessage) absMessage, maxSize);
                String name = getFileName((TLMessage) absMessage);
                if ((name != null) && (bytes != null)){
                    String filePath = FileMethods.setFileNameAndPath(name, path);
                    FileMethods.writeBytesToFile(filePath, bytes);
                    out = filePath;
                    System.err.println(((TLMessage) absMessage).getId()+" "+bytes.length + " " + name);
                }
            }
        }
        return out;
    }

    /**
     * Gets the file name and bytes, saves to DB, in case of success returns the resulting filepath
     * @param api api
     * @param absMessage message
     * @param maxSize max size of medial to be saved
     */
    public static String messageDownloadMediaToDB(TelegramApi api, DBStorage dbStorage, TLAbsMessage absMessage, int maxSize) {
        String out = null;
        if (absMessage instanceof TLMessage) {
            if (((TLMessage) absMessage).hasMedia()) {
                byte[] bytes = messageGetMediaBytes(api, (TLMessage) absMessage, maxSize);
                String name = getFileName((TLMessage) absMessage);
                if ((name != null) && (bytes != null)){
                    out = name;
                    dbStorage.writeFile(name, bytes);
                    System.err.println(((TLMessage) absMessage).getId()+" "+bytes.length + " " + name);
                }
            }
        }
        return out;
    }

    /**
     * downloads photos and documents
     * @param api api
     * @param message abstract message
     * @param maxSize max size of downloadable document
     * @see TLAbsMessage
     * @see TLAbsMessageMedia
     */
    private static byte[] messageGetMediaBytes(TelegramApi api, TLMessage message, int maxSize) {
        byte[] output = null;
        // check for media
        TLAbsMessageMedia absMedia = message.getMedia();
        if (absMedia instanceof TLMessageMediaDocument) {
            output = messageMediaDocumentGet(api, (TLMessageMediaDocument) absMedia, maxSize);
        } else if (absMedia instanceof TLMessageMediaPhoto) {
            output = messageMediaPhotoGet(api, (TLMessageMediaPhoto) absMedia, maxSize);
        }
        return output;
    }


    /**
     * get bytes of the doc if it exists and doesn't exceed the maxSize
     * @param	mediaDocument document media
     * @see	TLMessageMediaDocument
     */
    private static byte[] messageMediaDocumentGet(TelegramApi api, TLMessageMediaDocument mediaDocument, int maxSize){
        TLAbsDocument absDoc = mediaDocument.getDocument();
        byte[] bytes = null;

        if (absDoc instanceof TLDocument){
            TLDocument doc = (TLDocument) absDoc;
            TLInputDocumentFileLocation inputDocFileLoc = SetTLObjectsMethods.inputDocumentFileLocationSet(doc.getId(), doc.getAccessHash(), doc.getVersion());
            int fileDc = doc.getDcId();
            bytes = getFileBytesLoop(api, inputDocFileLoc, fileDc, doc.getSize(), maxSize);
        }
        return bytes;
    }

    /**
     * get bytes of photo if it exists and doesn't exceed the maxSize
     * @param api api
     * @param mediaPhoto photo media
     * @param maxSize max size for downloading
     */
    private static byte[] messageMediaPhotoGet(TelegramApi api, TLMessageMediaPhoto mediaPhoto, int maxSize){
        byte[] bytes = null;

        TLAbsPhoto absPhoto = mediaPhoto.getPhoto();
        if (absPhoto instanceof TLPhoto){
            TLPhoto photo = (TLPhoto) absPhoto;
            TLVector<TLAbsPhotoSize> absPhotoSizes = photo.getSizes();
            // last photo size - largest one
            TLPhotoSize photoSize = FileMethods.getLargestAvailablePhotoSize(absPhotoSizes);
            bytes = messagePhotoSizeOutput(api, photoSize, maxSize);
        }
        return bytes;
    }

    /**
     * Outputs web page media in console
     * @param	photoSize   TLPhotoSize instance
     * @see	TLPhotoSize
     * @see TLPhoto
     */
    private static byte[] messagePhotoSizeOutput(TelegramApi api, TLPhotoSize photoSize, int maxSize){
        byte[] bytes = null;
        // file location
        TLAbsFileLocation absFileLoc = photoSize.getLocation();
        if (absFileLoc instanceof TLFileLocation){
            TLFileLocation fileLoc = (TLFileLocation) absFileLoc;
            // set location, get Dc
            int fileDc = fileLoc.getDcId();
            TLInputFileLocation inputFileLoc = SetTLObjectsMethods.inputFileLocationSet(fileLoc.getLocalId(), fileLoc.getSecret(), fileLoc.getVolumeId());
            bytes = getFileBytesLoop(api, inputFileLoc, fileDc, photoSize.getSize(), maxSize);
        }
        return bytes;
    }

    /**
     * retrieves byte[] of a file
     * @param api api
     * @param inputFileLoc abs file loc
     * @param fileDc dc
     * @param realSize real size
     * @param maxSize threshold
     */
    private static byte[] getFileBytesLoop(TelegramApi api, TLAbsInputFileLocation inputFileLoc, int fileDc, int realSize, int maxSize){
        byte[] bytes = null;
        if ((realSize <= maxSize) && (realSize <= 1572864000)){
            Integer partSize = FileMethods.getFilePartSize(realSize);
            partSize *= 1024; // set partSize to KBs
            int offset = 0;
            try {
                while (offset < realSize) {
                    // get the part of the file
                    TLAbsFile absFile = api.doGetFile(fileDc, inputFileLoc, offset, partSize);
                    // append bytes to file
                    if (absFile instanceof TLFile){
                        TLFile file = (TLFile) absFile;
                        bytes = FileMethods.concatenateByteArrays(bytes, file.getBytes().getData());
                    }
                    offset += partSize;
                }
            } catch (RpcException e) {
                System.err.println(e.getErrorTag() + " " + e.getErrorCode());
            } catch (TimeoutException | IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return bytes;
    }

    /**
     * returns the filename of message media (if exists)
     * @param message message
     */
    private static String getFileName(TLMessage message){
        String name = null;
        if (message.hasMedia()) {
            TLAbsMessageMedia absMedia = message.getMedia();
            if (absMedia instanceof TLMessageMediaDocument) {
                name = getDocName(message, ((TLMessageMediaDocument) absMedia).getDocument());
            } else if (absMedia instanceof TLMessageMediaPhoto) {
                name = getPhotoName(message, ((TLMessageMediaPhoto) absMedia).getPhoto());
            }
        }
        return name;
    }


    /**
     * Gets doc name (or creates from id of chat and msg and date)
     * @param message message with doc
     * @param absDoc  abs doc
     */
    private static String getDocName(TLMessage message, TLAbsDocument absDoc) {
        String name = null;
        if (absDoc instanceof TLDocument) {
            TLDocument doc = (TLDocument) absDoc;

            TLVector<TLAbsDocumentAttribute> docAttr = doc.getAttributes();
            for (TLAbsDocumentAttribute attr : docAttr) {
                if (attr instanceof TLDocumentAttributeFilename) {
                    name = ((TLDocumentAttributeFilename) attr).getFileName();
                }
            }
            if ((name != null) && !name.isEmpty()){
                return message.getChatId() + "_" + message.getId() + "_" + doc.getId() + "_" + doc.getDate() + " " + name;
            } else {
                name = message.getChatId() + "_" + message.getId() + "_" + doc.getId() + "_" + doc.getDate();
                for (TLAbsDocumentAttribute attr : docAttr) {
                    if (attr instanceof TLDocumentAttributeAudio) {
                        return name + ".ogg"; // audio message
                    } else if (attr instanceof TLDocumentAttributeVideo) {
                        return name + ".mp4"; // video message
                    } else if (attr instanceof TLDocumentAttributeAnimated) {
                        return name + ".gif"; // gif
                    } else if (attr instanceof TLDocumentAttributeSticker) {
                        return name + ".webp"; // sticker
                    }
                }
            }

        }
        return name;
    }

    /**
     * Creates photo name from id of chat and msg and date (photos are always .jpg)
     * @param message message
     * @param absPhoto photo
     */
    private static String getPhotoName(TLMessage message, TLAbsPhoto absPhoto) {
        String name = null;
        if (absPhoto instanceof TLPhoto){
            TLPhoto photo = (TLPhoto) absPhoto;
            name = message.getChatId() + "_" + message.getId() + "_" + photo.getId() + "_" + photo.getDate()+".jpg";
        }
        return name;
    }

}
