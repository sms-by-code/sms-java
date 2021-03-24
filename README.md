# startsend-java-sms
Смс рассылка на java. Двухфакторная авторизация на Java. 

StartSend - класс, который упрощает работу с API StartSend. 
Качаете, указываете токен и можете сразу делать смс рассылку как одиночную, так и для массовых сообщений. 

StartSendSample - множество примеров работы с API StartSend, в том числе двухфакторная авторизация. 

Для того, чтобы настроить двухфакторную авторизацию вам нужно создать PasswordObject. 


    PasswordObject - это настройки, которые вы можете использовать в двухфактороной верификации
     Например вам надо, чтобы пароль состоял только из только букв или только цифр или и то и другое, с длиной пароля в 5 символов.


     Создание таких настройке выгдялит следующим образом:

     // настройка длины пароля при двухфакторной авторизации с длиной пароля в 5 символов.

         createPasswordObject(StartSend.PASS_TYPE_LETTERS, 5) ; // только буквы латиницы
         createPasswordObject(StartSend.PASS_TYPE_NUMBERS, 5) ; // только цифры
         createPasswordObject(StartSend.PASS_TYPE_BOTH, 5) ;    // и буквы и цифры

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
 
 Параметры метода: 
 public String createPasswordObject(String type, Integer len)
