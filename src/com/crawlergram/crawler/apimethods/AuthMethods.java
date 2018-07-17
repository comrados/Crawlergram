/*
 * Title: CrawlerMain.java
 * Project: telegramJ
 * Creator: Georgii Mikriukov
 */

package com.crawlergram.crawler.apimethods;

import org.telegram.api.TLConfig;
import org.telegram.api.TLNearestDc;
import org.telegram.api.auth.TLAuthorization;
import org.telegram.api.auth.TLSentCode;
import org.telegram.api.engine.RpcException;
import org.telegram.api.engine.TelegramApi;
import org.telegram.api.engine.storage.AbsApiState;
import org.telegram.api.functions.auth.TLRequestAuthSendCode;
import org.telegram.api.functions.auth.TLRequestAuthSignIn;
import org.telegram.api.functions.auth.TLRequestAuthSignUp;
import org.telegram.api.functions.help.TLRequestHelpGetConfig;
import org.telegram.api.functions.help.TLRequestHelpGetNearestDc;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

public class AuthMethods {

    /**
     * Sets Api state. Gets nearest DC (essential for connection), DC config and sets api config
     * @param	api  TelegramApi instance for RPC request
     * @param	apiState  AbsApiState or MemoryApiState instance
     * @see TelegramApi
     * @see AbsApiState
     */
    public static void setApiState(TelegramApi api, AbsApiState apiState){
        try {
            final TLConfig config = api.doRpcCallNonAuth(new TLRequestHelpGetConfig());
            TLNearestDc nearestDc = api.doRpcCallNonAuth(new TLRequestHelpGetNearestDc());
            apiState.setPrimaryDc(nearestDc.getNearestDc());
            apiState.updateSettings(config);
        } catch (IOException | TimeoutException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Checks if already authorized. If not - signs in or signs up.
     * @param	api  TelegramApi instance for RPC request
     * @param	apiState  AbsApiState or MemoryApiState instance
     * @param   key api key from my.telegram.org
     * @param   hash    api hash from my.telegram.org
     * @param   phoneNum    phone number
     * @param   nameOpt    (optional) first name for signing up
     * @param   surnameOpt (optional) last name for signing up
     * @see TelegramApi
     * @see AbsApiState
     */
    public static void auth(TelegramApi api, AbsApiState apiState, int key,
                            String hash, String phoneNum, Optional<String> nameOpt, Optional<String> surnameOpt){
        String name = nameOpt.orElse("John");
        String surname = surnameOpt.orElse("Smith");
        try {
            TLAuthorization auth;
            // check if is not authenticated (for primary DC)
            if (!apiState.isAuthenticated()) {
                System.out.println("NOT AUTHENTICATED");
                // reset auth and try to auth again
                apiState.resetAuth();
                // requests auth code
                TLRequestAuthSendCode sendCode = SetTLObjectsMethods.sendCodeSet(hash, key, phoneNum);
                TLSentCode sentCode = api.doRpcCallNonAuth(sendCode);

                // if registered - sign in, else - sign up
                if (sentCode.isPhoneRegistered()) {
                    auth = singIn(api, phoneNum, sentCode.getPhoneCodeHash());
                } else {
                    auth = singUp(api, phoneNum, sentCode.getPhoneCodeHash(), name, surname);
                }
                // refresh api state
                apiState.doAuth(auth);
                apiState.setAuthenticated(apiState.getPrimaryDc(), true);
            }
            // output auth info
            System.out.println("AUTHENTICATED: " + apiState.getUserId());
        } catch (RpcException e) {
            System.err.println(e.getErrorTag() + " " + e.getErrorCode());
        } catch (TimeoutException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Sign in to Telegram
     * @param	api  TelegramApi instance for RPC request
     * @param   phoneNum    phone number
     * @param   codeHash    phoneCodeHash from the received auth request
     * @see TelegramApi
     * @see AbsApiState
     * @see TLAuthorization
     */
    private static TLAuthorization singIn(TelegramApi api, String phoneNum, String codeHash){
        TLAuthorization auth = null;
        try {
            TLRequestAuthSignIn signIn = SetTLObjectsMethods.signInSet(phoneNum, codeHash);
            auth = api.doRpcCallNonAuth(signIn);
        } catch (RpcException e) {
            System.err.println(e.getErrorTag() + " " + e.getErrorCode());
        } catch (TimeoutException e) {
            System.err.println(e.getMessage());
        }
        return auth;
    }

    /**
     * Sign up to Telegram
     * @param	api  TelegramApi instance for RPC request
     * @param   phoneNum    phone number
     * @param   codeHash    phoneCodeHash from the received auth request
     * @param   name    first name for signing up
     * @param   surname last name for signing up
     * @see TelegramApi
     * @see AbsApiState
     * @see TLAuthorization
     */
    private static TLAuthorization singUp(TelegramApi api, String phoneNum, String codeHash, String name, String surname){
        TLAuthorization auth = null;
        try {
            TLRequestAuthSignUp signUp = SetTLObjectsMethods.signUpSet(phoneNum, codeHash, name, surname);
            auth = api.doRpcCallNonAuth(signUp);
        } catch (RpcException e) {
            System.err.println(e.getErrorTag() + " " + e.getErrorCode());
        } catch (TimeoutException e) {
            System.err.println(e.getMessage());
        }
        return auth;
    }

}
