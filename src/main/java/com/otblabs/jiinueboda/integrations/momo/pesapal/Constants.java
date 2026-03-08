package com.otblabs.jiinueboda.integrations.momo.pesapal;

public class Constants {

    //test Kenyan Merchant;
    public  static String test_consumer_key ="qkio1BGGYAXTu2JOfm7XSXNruoZsrqEW";
    public  static String test_consumer_secret= "osGQ364R49cXKeOYSpaOnT++rHs=";

    public static String BASE_URL_SANDBOX ="https://cybqa.pesapal.com/pesapalv3/api";
    public static String BASE_URL_LIVE ="https://pay.pesapal.com/v3/api";

    public static String Auth_URL_SANDBOX =BASE_URL_SANDBOX+"/Auth/RequestToken";
    public static String Auth_URL_LIVE =BASE_URL_LIVE+"/Auth/RequestToken";

    public static String REGISTER_IPN_URL_SANDBOX =BASE_URL_SANDBOX+"/URLSetup/RegisterIPN";
    public static String REGISTER_IPN_URL_LIVE =BASE_URL_LIVE+"/URLSetup/RegisterIPN";

    public static String SUBMIT_ORDER_URL_SANDBOX =BASE_URL_SANDBOX+"/Transactions/SubmitOrderRequest";
    public static String SUBMIT_ORDER_URL_LIVE =BASE_URL_LIVE+"/Transactions/SubmitOrderRequest";



}
