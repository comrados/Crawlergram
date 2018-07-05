/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package old;

import crawler.apimethods.DialogsHistoryMethods;
import crawler.apimethods.SetTLObjectsMethods;
import crawler.output.ConsoleOutputMethods;
import crawler.output.FileMethods;
import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.document.TLAbsDocument;
import org.telegram.api.document.TLDocument;
import org.telegram.api.document.attribute.*;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.file.location.TLAbsFileLocation;
import org.telegram.api.file.location.TLFileLocation;
import org.telegram.api.game.TLGame;
import org.telegram.api.geo.point.TLAbsGeoPoint;
import org.telegram.api.geo.point.TLGeoPoint;
import org.telegram.api.input.filelocation.TLInputDocumentFileLocation;
import org.telegram.api.input.filelocation.TLInputFileLocation;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.message.TLMessage;
import org.telegram.api.message.media.*;
import org.telegram.api.photo.TLAbsPhoto;
import org.telegram.api.photo.TLPhoto;
import org.telegram.api.photo.size.TLAbsPhotoSize;
import org.telegram.api.photo.size.TLPhotoCachedSize;
import org.telegram.api.photo.size.TLPhotoSize;
import org.telegram.api.upload.file.TLAbsFile;
import org.telegram.api.upload.file.TLFile;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.webpage.TLAbsWebPage;
import org.telegram.api.webpage.TLWebPage;
import org.telegram.tl.TLVector;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class MessagesGetMediaMethods {

    /**
     * Outputs messages from dialogs in console and saves media
     * @param	api  TelegramApi instance for RPC request
     * @param   dialogs dialogs TLVector
     * @param   chatsHashMap    chats hashmap
     * @param   usersHashMap    users hashmap
     * @param   limit   maximum number of retrieved messages from each dialog (0 if no limit)
     * @param   path    path for the files saving
     * @see TelegramApi
     */
    public static void saveMediaFromDialogsMessages(TelegramApi api,
                                                    TLVector<TLDialog> dialogs,
                                                    HashMap<Integer, TLAbsChat> chatsHashMap,
                                                    HashMap<Integer, TLAbsUser> usersHashMap,
                                                    TLAbsMessage topMessage,
                                                    int limit, String path, int maxDate, int minDate) {
        for (TLDialog dialog : dialogs) {
            // make actions upon each message in loop
            TLVector<TLAbsMessage> messages = DialogsHistoryMethods.getWholeMessagesHistory(api, dialog, chatsHashMap, usersHashMap, topMessage, limit, maxDate, minDate);
            for (TLAbsMessage message : messages) {
                // write the message content in console
                ConsoleOutputMethods.testMessageOutputConsole(message);
                // save message media to file
                messageDownloadMedia(api, message, path);
            }
            // sleep between transmissions to avoid flood wait
            try {Thread.sleep(1000);} catch (InterruptedException ignored) {}
        }
    }

    /**
     * Downloads or outputs media in console from message
     * @param	absMessage  abstract message
     * @see TLAbsMessage
     * @see TLAbsMessageMedia
     */
    public static void messageDownloadMedia(TelegramApi api, TLAbsMessage absMessage, String path) {
        // check for message is not TLMessageEmpty
        if (absMessage instanceof TLMessage) {
            TLMessage message = (TLMessage) absMessage;
            // check for media
            if (message.hasMedia()) {
                TLAbsMessageMedia absMedia = message.getMedia();
                // contact from the phone book
                if (absMedia instanceof TLMessageMediaContact) {
                    messageMediaContactOutputConsole((TLMessageMediaContact) absMedia);
                    // document (files)
                } else if (absMedia instanceof TLMessageMediaDocument) {
                    messageMediaDocumentOutput(api, (TLMessageMediaDocument) absMedia, path);
                    // empty media
                } else if (absMedia instanceof TLMessageMediaEmpty) {
                    System.out.println("EMPTY MEDIA");
                    // game
                } else if (absMedia instanceof TLMessageMediaGame) {
                    messageMediaGameOutputConsole((TLMessageMediaGame) absMedia);
                    // geolocation coordinates
                } else if (absMedia instanceof TLMessageMediaGeo) {
                    messageMediaGeoOutputConsole((TLMessageMediaGeo) absMedia);
                    // photo
                } else if (absMedia instanceof TLMessageMediaPhoto) {
                    messageMediaPhotoOutput(api, (TLMessageMediaPhoto) absMedia, path);
                    // unsupported media
                } else if (absMedia instanceof TLMessageMediaUnsupported) {
                    System.err.println("UNSUPPORTED MEDIA TYPE");
                    // venue media (geolocation coordinates + description)
                } else if (absMedia instanceof TLMessageMediaVenue) {
                    messageMediaVenueOutputConsole((TLMessageMediaVenue) absMedia);
                    // web page media
                } else if (absMedia instanceof TLMessageMediaWebPage) {
                    messageMediaWebPageOutputConsole((TLMessageMediaWebPage) absMedia);
                    // invoice
                } else if (absMedia instanceof TLMessageMediaInvoice) {
                    messageMediaInvoiceOutputConsole((TLMessageMediaInvoice) absMedia);
                    // other media (unknown)
                } else {
                    System.err.println("UNKNOWN MEDIA");
                }
            }
        }
    }

    /**
     * Outputs contact media in console
     * @param	contact contact media
     * @see	TLMessageMediaContact
     */
    private static void messageMediaContactOutputConsole(TLMessageMediaContact contact){
        System.out.println("CONTACT: " + " " + contact.getPhoneNumber() + " " + contact.getUserId() + " " + contact.getFirstName() + " " + contact.getLastName());
    }

    /**
     * Outputs document media in console & saves to HDD
     * @param	mediaDocument document media
     * @see	TLMessageMediaDocument
     */
    private static void messageMediaDocumentOutput(TelegramApi api, TLMessageMediaDocument mediaDocument, String path){
        TLAbsDocument absDoc = mediaDocument.getDocument();
        if (absDoc instanceof TLDocument){
            TLDocument doc = (TLDocument) absDoc;
            String name = getDocFileNameType(doc);
            String filename = path + File.separator + "docs" + File.separator + doc.getId() + " " + name;
            TLInputDocumentFileLocation inputDocFileLoc = SetTLObjectsMethods.inputDocumentFileLocationSet(doc.getId(), doc.getAccessHash(), doc.getVersion());

            int fileDc = doc.getDcId();
            try {
                // calculate optimal part size
                Integer partSize = FileMethods.getFilePartSize(doc.getSize());
                if (!partSize.equals(null)){
                    partSize *= 1024; // set size from KB to bytes
                    int offset = 0;

                    while (offset < doc.getSize()) {
                        // get the part of the file
                        TLAbsFile absFile = api.doGetFile(fileDc, inputDocFileLoc, offset, partSize);
                        // append bytes to file
                        if (absFile instanceof TLFile){
                            TLFile file = (TLFile) absFile;
                            FileMethods.appendBytesToFile(filename, file.getBytes().getData());
                        }
                        offset += partSize;
                    }

                    System.out.println("DOCEMENT" + doc.getId() + " " + name);
                } else {
                    System.out.println("DOCUMENT: " + doc.getId() + " FILE TOO BIG");
                }
            } catch (IOException | TimeoutException e) {
                System.err.println(e.getMessage());
            }


        } else { // instance of TLDocumentEmpty
            System.out.println("DOCUMENT EMPTY");
        }
    }

    /**
     * Outputs game media in console
     * @param	mediaGame game media
     * @see	TLMessageMediaGame
     */
    private static void messageMediaGameOutputConsole(TLMessageMediaGame mediaGame){
        TLGame game = mediaGame.getGame();
        System.out.println("GAME: " + game.getTitle() + " " + game.getDescription());
    }

    /**
     * Outputs geo media in console
     * @param	mediaGeo geo media
     * @see	TLMessageMediaGeo
     */
    private static void messageMediaGeoOutputConsole(TLMessageMediaGeo mediaGeo){
        TLAbsGeoPoint absGeo = mediaGeo.getGeo();
        if (absGeo instanceof TLGeoPoint){
            TLGeoPoint geo = (TLGeoPoint) absGeo;
            System.out.println("LOCATION: lat: " + geo.getLat() + " lon: " + geo.getLon());
        } else { // instanceof TLGeoPointEmpty
            System.out.println("EMPTY LOCATION");
        }
    }

    /**
     * Outputs photo media in console & saves to HDD
     * @param	mediaPhoto  photo media
     * @see	TLMessageMediaPhoto
     */
    private static void messageMediaPhotoOutput(TelegramApi api, TLMessageMediaPhoto mediaPhoto, String path){
        TLAbsPhoto absPhoto = mediaPhoto.getPhoto();
        if (absPhoto instanceof TLPhoto){
            TLPhoto photo = (TLPhoto) absPhoto;
            TLVector<TLAbsPhotoSize> absPhotoSizes = photo.getSizes();
            // all photo sizes
            for (TLAbsPhotoSize absPhotoSize: absPhotoSizes){
                if (absPhotoSize instanceof TLPhotoSize){
                    messagePhotoSizeOutput(api,(TLPhotoSize) absPhotoSize, photo, path);
                    //cached photo (small ones)
                } else if (absPhotoSize instanceof TLPhotoCachedSize) {
                    messagePhotoSizeCachedOutput((TLPhotoCachedSize) absPhotoSize, photo, path);
                } else { // instance of TLPhotoEmpty
                    System.out.println("PHOTO SIZES EMPTY");
                }
            }
        } else { // instanceof TLPhotoEmpty
            System.out.println("EMPTY PHOTO");
        }
    }

    /**
     * Outputs venue media in console
     * @param	mediaVenue venue media
     * @see	TLMessageMediaVenue
     */
    private static void messageMediaVenueOutputConsole(TLMessageMediaVenue mediaVenue){
        System.out.println("VENUE: " + " " +  mediaVenue.getVenue_id() + " " + mediaVenue.getTitle() +
                " " + mediaVenue.getAddress() + " " + mediaVenue.getProvider());
        TLAbsGeoPoint absGeo = mediaVenue.getGeo();
        if (absGeo instanceof  TLGeoPoint){
            TLGeoPoint geo = (TLGeoPoint) absGeo;
            System.out.println("VENUE LOCATION: lat: " + geo.getLat() + " lon: " + geo.getLon());
        } else { // instanceof TLGeoPointEmpty
            System.out.println("EMPTY VALUE LOCATION");
        }
    }

    /**
     * Outputs invoice media in console     *
     * @param    mediaInvoice web page media
     * @see    TLMessageMediaWebPage
     */
    private static void messageMediaInvoiceOutputConsole(TLMessageMediaInvoice mediaInvoice) {
        System.out.println("INVOICE: " + mediaInvoice.getTotalAmount() + " " + mediaInvoice.getCurrency() + " " +
                mediaInvoice.getTitle() + " " + mediaInvoice.getDescription());
    }

    /**
     * Outputs web page media in console
     * @param	mediaWebPage web page media
     * @see	TLMessageMediaWebPage
     */
    private static void messageMediaWebPageOutputConsole(TLMessageMediaWebPage mediaWebPage){
        TLAbsWebPage absWebPage = mediaWebPage.getWebPage();
        if (absWebPage instanceof TLWebPage){
            TLWebPage webPage = (TLWebPage) absWebPage;
            System.out.println("WEB PAGE: " + webPage.getTitle() + " " + webPage.getUrl() + " " + webPage.getDescription());
        }
    }

    /**
     * Outputs web page media in console
     * @param	photoSize   TLPhotoSize instance
     * @param   photo   TLPhoto instance, which contains photoSizeCached
     * @see	TLPhotoSize
     * @see TLPhoto
     */
    private static void messagePhotoSizeOutput(TelegramApi api, TLPhotoSize photoSize, TLPhoto photo, String path){
        // file location
        TLAbsFileLocation absFileLoc = photoSize.getLocation();
        if (absFileLoc instanceof TLFileLocation){
            TLFileLocation fileLoc = (TLFileLocation) absFileLoc;
            System.out.println("PHOTO: " + photo.getId() + " " + photoSize.getType() + " " + photoSize.getSize());
            // set location, get Dc
            int fileDc = fileLoc.getDcId();
            TLInputFileLocation inputFileLoc = SetTLObjectsMethods.inputFileLocationSet(fileLoc.getLocalId(), fileLoc.getSecret(), fileLoc.getVolumeId());
            try {
                // calculate optimal part size
                Integer partSize = FileMethods.getFilePartSize(photoSize.getSize());
                if (!partSize.equals(null)){
                    partSize *= 1024; // set size from KB to bytes
                    int offset = 0;

                    while (offset < photoSize.getSize()) {
                        // get the part of the file
                        TLAbsFile absFile = api.doGetFile(fileDc, inputFileLoc, offset, partSize);
                        // append bytes to file
                        if (absFile instanceof TLFile){
                            TLFile file = (TLFile) absFile;
                            String filename = path + File.separator + "img" + File.separator + String.valueOf(photo.getId()) + photoSize.getType()+ ".jpg";
                            FileMethods.appendBytesToFile(filename, file.getBytes().getData());
                        }
                        offset += partSize;
                    }

                } else {
                    System.out.println("PHOTO: " + photo.getId() + " " + photoSize.getType() + " FILE TOO BIG");
                }
            } catch (IOException | TimeoutException e) {
                System.err.println(e.getMessage());
            }
        } else { // instance of TLFileLocationUnavailable
            System.out.println("PHOTO LOCATION UNAVAILABLE " + photo.getId() + " " + photoSize.getType());
        }
    }

    /**
     * Outputs cached photo in file and console
     * @param	photoSizeCached   TLPhotoCachedSize instance
     * @param   photo   TLPhoto instance, which contains photoSizeCached
     * @see	TLPhotoCachedSize
     * @see TLPhoto
     */
    private static void messagePhotoSizeCachedOutput(TLPhotoCachedSize photoSizeCached, TLPhoto photo, String path){
        System.out.println("PHOTO CACHED: " + photo.getId() + " " + photoSizeCached.getType() + " " + photoSizeCached.getBytes().getLength());
        String filename = path + File.separator + "img" + File.separator + String.valueOf(photo.getId()) + photoSizeCached.getType()+ "." + "jpg";
        FileMethods.writeBytesToFile(filename, photoSizeCached.getBytes().getData());
    }

    /**
     * Returns file name type
     * @param   doc document from message
     * @return string with file name type
     */
    private static String getDocFileNameType(TLDocument doc){
        String name = "";
        TLVector<TLAbsDocumentAttribute> docAttr = doc.getAttributes();
        for (TLAbsDocumentAttribute attr: docAttr) {
            if (attr instanceof TLDocumentAttributeFilename) {
                name = ((TLDocumentAttributeFilename) attr).getFileName();
            }
        }
        if (name.isEmpty()) {
            name = doc.getId()+"_document";
            for (TLAbsDocumentAttribute attr : docAttr) {
                if (attr instanceof TLDocumentAttributeAudio) {
                    name = doc.getId()+"_audio.ogg"; // audio message
                    return name;
                } else if (attr instanceof TLDocumentAttributeVideo) {
                    name = doc.getId()+"_video.mp4"; // video message
                    return name;
                } else if (attr instanceof TLDocumentAttributeAnimated) {
                    name = doc.getId()+"_anim.gif"; // //gif
                    return name;
                } else if (attr instanceof TLDocumentAttributeSticker) {
                    name = doc.getId()+"_sticker.webp";
                    return name;
                }
            }
        }
        return name;
    }

}
