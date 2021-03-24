import java.io.InputStream;
import java.io.File;
import java.lang.Process;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.String;
import java.net.URLEncoder;
import java.util.*;
import java.nio.charset.StandardCharsets;
import java.io.UnsupportedEncodingException;
import java.lang.NullPointerException;
import com.google.gson.*;

/**
    Class to send sms through SMS.BY sms/viber/voice service
    Compilating using command line:  javac -cp $CLASSPATH SMSBY_Sample.java
*/

class SMS_BY
{
    private String token;

    private String API_URL = "https://app.sms.by/api/v1/";
    private String m_URL   = "https://app.sms.by/";

    public final String PASS_TYPE_LETTERS = "letters";
    public final String PASS_TYPE_NUMBERS = "numbers";
    public final String PASS_TYPE_BOTH    = "both";


    /**
     *  token - API ключ
     */
    public SMS_BY(String token)
    {
       if (!token.isEmpty() )
          this.token = token;
      else {
          this.print("Код токена не указан. Вы можете получить его здесь: " + this.m_URL + "user-api/token");
          System.exit(-1);
      }
    }

    /**
     * Отправляет команду на API_URL.
     * Если команда обработана успешно, возвращает ответ от API в виде объекта.
     * Если команда обработана неуспешно - передаёт ошибку методу error() и возвращает false.
     *  command - команда API
     *  params - ассоциативный массив, ключи которого являются названиями параметров команды кроме token, значения - их значениями.
     *  token в params передавать не нужно.
     * Необязательный параметр, так как для таких команд, как getLimit, getMessagesList, getPasswordObjects никаких параметров передавать не нужно.
     */
    private String sendRequest(String command, Map <String, String> hParams)
    {
        String url = API_URL + command + "?token=" + this.token;
        String response = "";

        if ( !command.isEmpty() )
        {
            if (!hParams.isEmpty())
            {
                for (Map.Entry<String, String> pair : hParams.entrySet()) {
                    url += "&";
                    url += pair.getKey() + "=" + this.encodeValue(pair.getValue());
                }
            }
                // send request using CURL
                try{
                      String shellCommand = new String ("curl -X POST " + url);
                      this.print (shellCommand);

                      Process process = Runtime.getRuntime().exec(shellCommand);

                      String line     = "";

                      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                        while ((line = reader.readLine()) != null) {
                            System.out.println(line);
                            response += line;
                        }

                        JsonElement jelement = new JsonParser().parse(response);
                        JsonObject  jobject = jelement.getAsJsonObject();

                        if(jobject.has("error"))
                        {
                          jelement = jobject.get("error");
                          System.out.println(jelement.getAsString());
                          return null;
                        }

                    }
                    catch(IOException ex){
                            System.out.println (ex.toString());
                            System.out.println("Could not connect " );
                    }
          }
          return response;
        }


    /*
        Метод для отправки запросов без параметров
    */
    private String sendRequest(String command)
    {
        Map <String, String> hParams = new HashMap <String, String> ();
        return this.sendRequest(command,hParams);
    }

    /**
     * Метод-обёртка для команды sendSms
     * message - Текст созданного сообщения
     * phone - номер телефона в международном формате [country-code][operator][number], пример: 79061234567

       Sample output: {"sms_id":2197871,"status":"NEW"}
     */

    public String sendQuickSms(String message, String phone)
    {
      if(!message.isEmpty() && !phone.isEmpty())
      {
        Map <String, String> hParams = new HashMap <String, String> ();
        hParams.put ("message",message);
        hParams.put ("phone",phone);
        String response = this.sendRequest("sendQuickSms", hParams);

        return this.getJsonValueByKey("sms_id",response);
      }
      else
      {
         this.print("Номер телефона или сообщение пустое");
         return null;
      }
    }

  /**
   * Метод-обёртка для команды getLimit
     Пример ответа
      {"limit":141}
   */
  public String getLimit()
  {
      String response = this.sendRequest("getLimit");

      JsonElement jelement = new JsonParser().parse(response);
      JsonObject  jobject = jelement.getAsJsonObject();

      if(jobject.has("limit"))
      {
        jelement = jobject.get("limit");
        return jelement.getAsString();
      }
      return null;
  }

  /**
   * Метод-обёртка для команды createSMSMessage
   *  message - текст создаваемого сообщения
   *  alphaname_id - ID альфа-имени, необязательный параметр

     Пример возвращаемого значения:
       {"status":"ok","parts":1,"len":31,"message_id":2604834,"alphaname":"system","time":0}

       @return int message_id в случае успешного создания сообщения
   */
  public int createSMSMessage(String message, Integer  alphaname_id)
  {
    Map <String, String> hParams = new HashMap <String, String> ();
    hParams.put ("message",message);

      if (alphaname_id!=0)
          hParams.put ("alphaname_id", alphaname_id.toString());

      String response =  this.sendRequest("createSmsMessage", hParams);

      JsonElement jelement = new JsonParser().parse(response);
      JsonObject  jobject = jelement.getAsJsonObject();

      if(jobject.has("message_id"))
      {
        jelement = jobject.get("message_id");
        return jelement.getAsInt();
      }
      return 0;
  }

  /**
   * Метод-обёртка для команды checkSMSMessageStatus
   * message_id - ID созданного сообщения
     {"error":"not found"}
   */
  public String checkSMSMessageStatus(Integer message_id)
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("message_id",message_id.toString());
      return this.sendRequest("checkSMSMessageStatus", hParams);
  }

  /**
   * Метод-обёртка для команды getMessagesList

     Пример ответа:
      {"result":[
        {"message_id":2564327,"message":"68755","parts":1,"d_create":"2021-03-22 11:32:32","status":"moderated"},
        {"message_id":2564580,"message":"37271","parts":1,"d_create":"2021-03-22 11:32:32","status":"moderated"}
        ]
      }

   */
  public String getMessagesList()
  {
      return this.sendRequest("getMessagesList");
  }

  /**
   * Метод-обёртка для команды sendSms
   * message_id - ID созданного сообщения
   * phone - номер телефона в формате 375291234567
     Возвращает ID отправленного смс сообщения

     Пример ответа: {"sms_id":2204968,"status":"NEW"}
   */
  public String sendSms(Integer message_id, String phone)
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("message_id",message_id.toString());
      hParams.put ("phone",phone);

      String response =  this.sendRequest("sendSms", hParams);

      JsonElement jelement = new JsonParser().parse(response);
      JsonObject  jobject = jelement.getAsJsonObject();

      if(jobject.has("sms_id"))
      {
        jelement = jobject.get("sms_id");
        return jelement.getAsString();
      }
      return null;
  }

  /**
   * Метод-обёртка для команды checkSMS
   * sms_id - ID отправленного SMS
     Пример ответа:
        {"sms_id":2204968,"delivered":1616064780}
        {"sms_id":2204968,"sent":1616064780}
   */
  public String checkSms(String sms_id)
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("sms_id",sms_id);

      String response =  this.sendRequest("checkSMS", hParams);

      return response;
  }

  /**

    @param String sms_id  - ID смс-сообщения
    @return String status of sms
  */
  public String getSmsStatusById(String sms_id){

      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("sms_id",sms_id);

      String response =  this.sendRequest("checkSMS", hParams);
      if(null==this.getJsonValueByKey("sent",response))
      {
          if(this.getJsonValueByKey("delivered",response)!=null)
          {
            return "delivered";
          }
          else
            return "unknown status";// should not happen; or only if called immediately after sending
      }
      return "sent";
  }

  /**
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

   * Метод-обёртка для команды createPasswordObject
   * type_id - тип создаваемого объекта пароля, может принимать значения letters, numbers и both
   * len - длина создаваемого объекта пароля, целое число от 1 до 16
   *  Пример ответа: {"result":{"password_object_id":243}}

      @return: String - ID password Object
   */
  public String createPasswordObject(String type, Integer len)
  {

      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("type_id",type);
      hParams.put ("len",len.toString());

      String response = this.sendRequest("createPasswordObject", hParams);

      if(!response.isEmpty())
      {
        try {
              JsonElement jelement = new JsonParser().parse(response).getAsJsonObject().get("result");
                    String password_object_id = jelement.getAsJsonObject().get("password_object_id").getAsString();
                    return password_object_id;
              }
              catch(NullPointerException ex){
                return null;
              }
      }
        return null;
  }

  /**
   * Метод-обёртка для команды editPasswordObject
   *    password_object_id - ID созданного объекта пароля
   *    type_id - тип создаваемого объекта пароля, может принимать значения letters, numbers и both
   *    len - длина создаваемого объекта пароля, целое число от 1 до 16

      {"result":"1"}
      {"error":"len value must be beetwen 1 and 16"}

      return OK если редактирование прошло успешно и null в случае ошибки
   */
  public String editPasswordObject(String password_object_id,  String type_id, String len)
  {

      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("id",password_object_id);
      hParams.put ("type_id",type_id);
      hParams.put ("len",len);

      String response = this.sendRequest("editPasswordObject", hParams);

      if(response!=null)
      {
           response = this.getJsonValueByKey("result",response);

           if (1 == Integer.parseInt(response))
           {
             return "OK";
           }
           return null;

      }
      return null;
  }

  /**
   * Метод-обёртка для команды deletePasswordObject
   * password_object_id - ID созданного объекта пароля
      {"result":"1"}
      {"error":"not found"}
   */
  public String deletePasswordObject(String password_object_id)
  {

      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("id",password_object_id);

      String response = this.sendRequest("deletePasswordObject", hParams);

      if(response!=null)
      {
           response = this.getJsonValueByKey("result",response);

           if (1 == Integer.parseInt(response))
           {
             return "OK";
           }
           return null;

      }
      return null;
  }

  /**
     Метод-обёртка для команды getPasswordObjects
     Пример ответа:
        {"result":[{"id":232,"type_id":"both","len":5,"d_create":"2021-03-19 11:03:39"},
                   {"id":233,"type_id":"both","len":5,"d_create":"2021-03-19 11:03:39"}]}
   */
  public String getPasswordObjects()
  {
      return this.sendRequest("getPasswordObjects");
  }
  /**
   * Метод-обёртка для команды getPasswordObject
   * password_object_id - ID созданного объекта пароля

     Пример возвращаемого значения
        {"result":{"id":248,"type_id":"both","len":4,"d_create":"2021-03-20 14:50:11"}}
   */
  public String getPasswordObject(String password_object_id)
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("id",password_object_id.toString());

      return this.sendRequest("getPasswordObject", hParams);
  }

  /**
   * Метод-обёртка для команды sendSmsMessageWithCode
   * message - текст создаваемого сообщения
   * password_object_id - ID созданного объекта пароля
   * phone - номер телефона в формате 375291234567
   * alphaname_id - ID альфа-имени, необязательный параметр

     Пример ответа:
      [{"status":"ok","parts":1,"len":21,"sms_id":2208471,"code":"GAYXILYZOX"}]
   */
  public String sendSmsMessageWithCode(String message, String password_object_id, String phone, Integer alphaname_id )
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("message",message);
      hParams.put ("password_object_id",password_object_id);
      hParams.put ("phone",phone);

      if (alphaname_id!=0)
          hParams.put ("alphaname_id", alphaname_id.toString());

      return this.sendRequest("sendSmsMessageWithCode", hParams);
  }

  /**
   * Метод-обёртка для команды getAlphaNames ?

     Пример ответа: {"0":"Системное имя (SMS.BY)","1363":"VasheImya"}
   */
  public String getAlphaNames()
  {
      return this.sendRequest("getAlphanames");
  }

  /**
   * Метод-обёртка для команды getAlphaNameId
     Пример ответа: {"id":1111}
   */
  public Integer getAlphaNameId(String name)
  {
      Map <String, String> hParams = new HashMap <String, String> ();
      hParams.put ("name",name);
      String res = this.sendRequest("getAlphanameId", hParams);
      res = this.getJsonValueByKey("id",res);

      if(res !=null)
        return Integer.parseInt(res);
      return null;
  }


  /*
      Получение баланса:
      Sample output: {"status":"OK","currency":"RUR","result":[{"balance":"2.86600","viber_balance":"0.00000"}]}

      return Balance object
  */
  public Balance getBalance ()
  {
      String jsonLine =  this.sendRequest("getBalance");

      JsonElement jelement = new JsonParser().parse(jsonLine);
      JsonObject  jobject = jelement.getAsJsonObject();

      JsonArray jarray = jobject.getAsJsonArray("result");
      jobject = jarray.get(0).getAsJsonObject();

      String balance = jobject.get("balance").toString();
      String viber_balance = jobject.get("viber_balance").toString();

      if (!balance.isEmpty() && !viber_balance.isEmpty()){
          Balance oBalance = new Balance(balance,viber_balance);
          return oBalance;
      }
      return null;
  }

  /**
   * Обрабатывает ошибки.
   * Здесь может быть любой код, обрабатывающий пришедшую по API ошибку, соответствующий вашему приложению.
   *  error - текст ошибки
   */
  private void error(String error)
  {
    this.print("<b>Error:</b> " + error);
    System.exit(-1);
  }


  public void print(String str)
  {
      System.out.println("");
      System.out.println(str);
      System.out.println("");
  }


 private String encodeValue(String value) {

    try {

          return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
    catch (UnsupportedEncodingException ex)
    {
          return ex.toString();
    }
  }

  public String getJsonValueByKey(String key, String response)
  {
    try {
       if  (!response.isEmpty())
       {
         JsonElement jelement = new JsonParser().parse(response);
         JsonObject  jobject = jelement.getAsJsonObject();

         if(jobject.has(key))
         {
           jelement = jobject.get(key);
           return jelement.getAsString();
         }
       }
     }
     catch (NullPointerException ex )
     {
         // this.print(ex.toString());
         return null;
     }
     return null;
  }

} // end of class SMS_BY



class Balance {

     private String balance;
     private String viberBalance;
     private String currency;

     public Balance ()
     {

     }

     public Balance (String balance, String viberBalance)
     {
         this.balance      = balance;
         this.viberBalance = viberBalance;
     }

     public String getBalance()
     {
       return this.balance;
     }
     public String getViberBalance(){
       return this.viberBalance;
     }
     public void setCurrency(String currency){
       this.currency=currency;
     }
     public String getCurrency(){
       return this.currency;
     }
}
