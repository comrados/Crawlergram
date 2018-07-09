/*
 * Title: ConsoleOutputMethods.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package crawler.output;

import org.telegram.api.chat.TLAbsChat;
import org.telegram.api.chat.TLChat;
import org.telegram.api.chat.TLChatEmpty;
import org.telegram.api.chat.TLChatForbidden;
import org.telegram.api.chat.channel.TLChannel;
import org.telegram.api.chat.channel.TLChannelForbidden;
import org.telegram.api.dialog.TLDialog;
import org.telegram.api.message.TLAbsMessage;
import org.telegram.api.message.TLMessage;
import org.telegram.api.message.TLMessageService;
import org.telegram.api.peer.TLPeerChannel;
import org.telegram.api.peer.TLPeerChat;
import org.telegram.api.peer.TLPeerUser;
import org.telegram.api.user.TLAbsUser;
import org.telegram.api.user.TLUser;
import java.util.Map;
import java.util.Set;

public class ConsoleOutputMethods {

    /**
     * Outputs peer type and id from dialog to console
     * @param	dialog  TLDialog instance with peer information
     * @see TLDialog
     */
    public static void testPeerOutputConsole(TLDialog dialog) {
        if (dialog.getPeer() instanceof TLPeerUser) {
            System.out.println(dialog.getPeer().getId() + " TLPeerUser");
        } else if (dialog.getPeer() instanceof TLPeerChannel) {
            System.out.println(dialog.getPeer().getId() + " TLPeerChannel");
        } else if (dialog.getPeer() instanceof TLPeerChat) {
            System.out.println(dialog.getPeer().getId() + " TLPeerChat");
        } else {
            System.out.println(dialog.getPeer().getId() + " " + dialog.getPeer().getClass().toString());
        }
        System.out.println();
    }

    /**
     * Outputs content of chats hashmap to console
     * @param	chatsHashMap  HashMap with chats
     * @see Map
     * @see Set
     * @see TLAbsChat
     */
    public static void testChatsHashMapOutputConsole(Map<Integer, TLAbsChat> chatsHashMap){
        Set<Integer> keysChats = chatsHashMap.keySet();
        for (Integer key : keysChats) {
            if (chatsHashMap.get(key) instanceof TLChannel) {
                System.out.println(chatsHashMap.get(key).getId() + " Channel " + ((TLChannel) chatsHashMap.get(key)).getTitle() + " " + ((TLChannel) chatsHashMap.get(key)).getFlags());
            } else if (chatsHashMap.get(key) instanceof TLChat) {
                System.out.println(chatsHashMap.get(key).getId() + " Chat " + ((TLChat) chatsHashMap.get(key)).getTitle() + " " + ((TLChat) chatsHashMap.get(key)).getFlags());
            } else if (chatsHashMap.get(key) instanceof TLChatForbidden) {
                System.out.println(chatsHashMap.get(key).getId() + " ChatForbidden " + ((TLChatForbidden) chatsHashMap.get(key)).getTitle());
            } else if (chatsHashMap.get(key) instanceof TLChatEmpty) {
                System.out.println(chatsHashMap.get(key).getId() + " ChatEmpty");
            } else if (chatsHashMap.get(key) instanceof TLChannelForbidden) {
                System.out.println(chatsHashMap.get(key).getId() + " ChannelForbidden " + ((TLChannelForbidden) chatsHashMap.get(key)).getTitle());
            } else {
                System.out.println(chatsHashMap.get(key).getId() + "Other");
            }
        }
        System.out.println();
    }

    /**
     * Outputs content of users hashmap to console
     * @param	usersHashMap  HashMap with users
     * @see Map
     * @see Set
     * @see TLAbsUser
     */
    public static void testUsersHashMapOutputConsole(Map<Integer, TLAbsUser> usersHashMap){
        Set<Integer> keysUsers = usersHashMap.keySet();
        for (Integer key : keysUsers) {
            System.out.println(usersHashMap.get(key).getId() + " User " + ((TLUser) usersHashMap.get(key)).getUserName() + " " +
                    ((TLUser) usersHashMap.get(key)).getFirstName() + " " + ((TLUser) usersHashMap.get(key)).getLastName() + " " + ((TLUser) usersHashMap.get(key)).getFlags());
        }
        System.out.println();
    }

    public static String testOutHashMapObjectByKey(int key , Map<Integer, TLAbsChat> chatsHashMap, Map<Integer, TLAbsUser> usersHashMap){
        String out = "";
        Set<Integer> keysUsers = usersHashMap.keySet();
        Set<Integer> keysChats = chatsHashMap.keySet();
        if (keysUsers.contains(key)) {
           out = usersHashMap.get(key).getId() + " User " + ((TLUser) usersHashMap.get(key)).getUserName() + " " +
                    ((TLUser) usersHashMap.get(key)).getFirstName() + " " + ((TLUser) usersHashMap.get(key)).getLastName() + " " + ((TLUser) usersHashMap.get(key)).getFlags();
        } else if (keysChats.contains(key)) {
            if (chatsHashMap.get(key) instanceof TLChannel) {
                out =chatsHashMap.get(key).getId() + " Channel " + ((TLChannel) chatsHashMap.get(key)).getTitle() + " " + ((TLChannel) chatsHashMap.get(key)).getFlags();
            } else if (chatsHashMap.get(key) instanceof TLChat) {
                out =chatsHashMap.get(key).getId() + " Chat " + ((TLChat) chatsHashMap.get(key)).getTitle() + " " + ((TLChat) chatsHashMap.get(key)).getFlags();
            } else if (chatsHashMap.get(key) instanceof TLChatForbidden) {
                out =chatsHashMap.get(key).getId() + " ChatForbidden " + ((TLChatForbidden) chatsHashMap.get(key)).getTitle();
            } else if (chatsHashMap.get(key) instanceof TLChatEmpty) {
                out =chatsHashMap.get(key).getId() + " ChatEmpty";
            } else if (chatsHashMap.get(key) instanceof TLChannelForbidden) {
                out =chatsHashMap.get(key).getId() + " ChannelForbidden " + ((TLChannelForbidden) chatsHashMap.get(key)).getTitle();
            } else {
                out =chatsHashMap.get(key).getId() + "Other";
            }
        }
        return out;
    }

    /**
     * Outputs message text from message to console
     * @param	absMessage  abstract message
     * @see TLAbsMessage
     * @see TLMessage
     */
    public static void testMessageOutputConsole(TLAbsMessage absMessage) {
        if (absMessage instanceof TLMessage) {
            System.out.println(((TLMessage) absMessage).getMessage());
        } else if (absMessage instanceof TLMessageService){
            System.out.println(((TLMessageService) absMessage).getAction());
        } else {
            System.out.println("Empty Message");
        }
    }





}
