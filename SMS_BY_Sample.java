import java.io.InputStream;
import java.io.File;
import java.lang.Process;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import com.google.gson.*;


/*
      Список требуемых файлов:
      A list of necessary files: 
      SMS_BY.java
      Transliterate.java
      gson-2.2.4.jar

*/

class SMS_BY_Sample
{

  private static String phone   = "";
  //
  private static String token   = "" ; // token

  public static String parse(String jsonLine)
  {
     JsonElement jelement = new JsonParser().parse(jsonLine);
     JsonObject  jobject = jelement.getAsJsonObject();

     if(jobject.has("error"))
     {
        SMS_BY_Sample.print("has error");
        jelement = jobject.get("error");
        return jelement.getAsString();
     }
     else return "no error";

  }


  public static void printValues(Map<String, String> map)
  {
      System.out.println("===============JSON OUT===================");

      for(Map.Entry<String, String> pair : map.entrySet())
      {
          String value = pair.getValue();
          System.out.println(value);
      }
      System.out.println("===============JSON OUT===================");
  }


  public static void print(String str)
  {
      System.out.println(str);
  }


  private static String encodeValue(String value) {

    try {
          return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
    catch (UnsupportedEncodingException ex)
    {
        return ex.toString();
    }
  }


  public static void main(String[] args )
  {

      String str = "";
      String res = "";

     // createSmsMessage
     // checkSMSMessageStatus
     SMS_BY oStart = new SMS_BY(SMS_BY_Sample.token);

     // получение баланса
     // Get Balance 
     SMS_BY_Sample.print("Получаем баланс:");
     SMS_BY_Sample.print("Get balance:");
     Balance balance = new Balance();
             balance = oStart.getBalance();

     SMS_BY_Sample.print("balance="+balance.getBalance() + "viber="+balance.getViberBalance());

     // Get message limits   
     SMS_BY_Sample.print("Получаем лимит на отправку сообщений:");
     res = oStart.getLimit();
     // Limit is  
     SMS_BY_Sample.print("Лимит = " + res);

     // Простая отправка и проверка статуса смс
     // Send quick sms  
     // sendQuickSms
     // checkSms
     // Sample message text  
     String message = "Привет от SMS.BY! Hi From Sms.by";
     String sms_id  = oStart.sendQuickSms(message,SMS_BY_Sample.phone);

     // Статус отправки смс
     // Check Sms Message Status  
     SMS_BY_Sample.print("Проверяем статус отправки смс-сообщения:  ");
     SMS_BY_Sample.print("Check sms message status:  ");
     try{
       Thread.sleep(1500);
     }
     catch (InterruptedException ex )
     {
       SMS_BY_Sample.print(ex.toString());
     }
     res = oStart.getSmsStatusById(sms_id);
     SMS_BY_Sample.print("Статус отправки смс с ID = " + sms_id + " = " + res);

    message = "Привет от SMS.BY! Powered by Java.";
    // create Sms Campaign  
    int message_id = oStart.createSMSMessage(message,0);
    res = String.valueOf(message_id);
    SMS_BY_Sample.print("Создана СМС-рассылка с ID = " + res );
    res = oStart.sendSms(message_id,SMS_BY_Sample.phone);


    SMS_BY_Sample.print("Результат отправки сообщения  = " + message + "на номер:" + SMS_BY_Sample.phone );
    SMS_BY_Sample.print("Delivery status of  = " + message + "to number:" + SMS_BY_Sample.phone );
    SMS_BY_Sample.print(res);

    SMS_BY_Sample.print("Проверяем статус рассылки ID = " + message_id );
    SMS_BY_Sample.print("Check SMS Campaign by ID = " + message_id );
    res = oStart.checkSMSMessageStatus(message_id);

    try{
      Thread.sleep(1500);
    }
    catch (InterruptedException ex )
    {
      SMS_BY_Sample.print(ex.toString());
    }

    SMS_BY_Sample.print(res);


    res = Transliterate.cyr2lat("абвгдеёжзийклмнопрстуфхцшщъыьюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦШЩЪЫЬЮЯ");
    // System.exit(1);
    SMS_BY_Sample.print("Пример транслитерации строки:");
    // useful method if you want to cut cost on long Russian sms messages   
    SMS_BY_Sample.print("Tranliteration sample from Russian into English:");
    SMS_BY_Sample.print(res);

    // ----
    // Get a list of available Sender ID's 
    SMS_BY_Sample.print("Получаем список доступных Альфа-имен:");
    SMS_BY_Sample.print("Get a list of available Sender ID's :");
      
    res = oStart.getAlphaNames();
    SMS_BY_Sample.print(res);

    SMS_BY_Sample.print("Получаем ID Альфа-имени  SMS.BY:");
    SMS_BY_Sample.print("Get Sender ID of SMS.BY (it is 0):");  
    Integer alphaNameId = oStart.getAlphaNameId("SMS.BY");

    if (alphaNameId!=null)
     SMS_BY_Sample.print("AlphaNameId для 'SMS.BY' = "  + alphaNameId.toString());


    // 2-х факторная авторизация
    /*
    Двухфакторная авторизация работает следующим образом.
    Вы настраиваете длину и конфигурацию пароля, после этого у вас будет ID.
    Далее по этому ID вы отправляете смс и в ответ вам приходит код, который отправляется пользователю.

    В нашей системе такая настройка реализуется через PasswordObject.

    PasswordObject - это настройки, которые вы можете использовать в двухфактороной верификации

    Например вам надо, чтобы пароль состоял только из только букв или только цифр или и то и другое, с длиной пароля в 5 символов.

     Создание таких настройке выгдялит следующим образом:

     // настройка длины пароля при двухфакторной авторизации с длиной пароля в 5 символов.

         createPasswordObject(SMS_BY.PASS_TYPE_LETTERS, 5) ; // только буквы латиницы
         createPasswordObject(SMS_BY.PASS_TYPE_NUMBERS, 5) ; // только цифры
         createPasswordObject(SMS_BY.PASS_TYPE_BOTH, 5) ;    // и буквы и цифры

    Чтобы отправить смс с кодом нужно сделать вызов:

      oStart.sendSmsMessageWithCode("Ваш пароль: %CODE%", "245", phone,alphaname_id  );

      "Ваш пароль: %CODE%" - текст сообщения, %CODE% - обязательный параметр, вместо %CODE% будет подставлен
      сгененированный пароль для получателя

      "245" - это ID PasswordObject созданный ранее
      phone - номер телефона
      alphaname_id - ID Альфа-имени, если Альфа-имени пока нет, нужно передавать 0

      sendSmsMessageWithCode вернет:
        {"status":"ok","parts":1,"len":21,"sms_id":2208471,"code":"GAYXILYZOX"}

      Из этого сообщения вам надо получить код, который будет вводить пользователь на форме двухфакторной авторизации
        code = GAYXILYZOX
    */
      
    /* 
     Send sms via SMS.by using Java. 2FA using Java

SMS_BY - class which simplifies integration with SMS.by API.

All you need is to download the code, specify your security token and test our examples to see how it works.

SMS_BY_Sample - contains many samples how to use SMS.by's API + two factor authorization (2FA).

In order to setup 2FA you need to create a PasswordObject.

PasswordObject - are settings which configure the password length and type of symbols to be used in that password: letter/digits or both


Password configuration:

 // Password configuration for 2FA, password length = 5 chars 


     createPasswordObject(SMS_BY.PASS_TYPE_LETTERS, 5) ; // Only latin charachters 
     createPasswordObject(SMS_BY.PASS_TYPE_NUMBERS, 5) ; // Only digits
     createPasswordObject(SMS_BY.PASS_TYPE_BOTH, 5) ;    // Letters & digits

  Then all you need is to text:

  oStart.sendSmsMessageWithCode("Your password is: %CODE%", "245", phone,alphaname_id  );

  "Your password: %CODE%" - message text
  %CODE% - mandatory parameter, we substitute %CODE% for generated password sent to a user 

  "245" -  PasswordObject's ID 
  phone - Recepient phone number
  alphaname_id - Sender ID, if 0 default Sender ID would be used

  a call to sendSmsMessageWithCode returns:
    {"status":"ok","parts":1,"len":21,"sms_id":2208471,"code":"GAYXILYZOX"}

  The code for 2FA to compare in your app is:
    code = GAYXILYZOX
Wrapper method for createPasswordObject
type_id - Password type to be used: letters, numbers or both
len - password length, integer between 1 and 16
 Response sample: {"result":{"password_object_id":243}} @return: String - ID password Object 
Method declaration public String createPasswordObject(String type, Integer len)
    
    */

    String password_object_id  = oStart.createPasswordObject("both",4);
    Integer alphaname_id       = new Integer(0);

    str = oStart.sendSmsMessageWithCode("Ваш пароль: %CODE%", password_object_id, SMS_BY_Sample.phone,alphaname_id  );
    SMS_BY_Sample.print(str);

    SMS_BY_Sample.print("Получаем список отправленных сообщений: ");
    SMS_BY_Sample.print("Get a list of send messages: ");
    res = oStart.getMessagesList();
    SMS_BY_Sample.print(res);

  }
}
