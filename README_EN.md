
Send sms via SMS.by using Java. 2FA using Java


SMS_BY - class which simplifies integration with SMS.by API. 

All you need is to download the code, specify your security token and test our examples to see how it works. 

SMS_BY_Sample - contains many samples how to use  SMS.by's API + two factor authorization (2FA). 

In order to setup 2FA you need to create a PasswordObject. 


    PasswordObject - are settings which configure the password length and type of symbols to be used in that password: letter/digits or both


    Password configuration:

     // Password configuration for 2FA, password length = 5 chars 
SMS_BY_Sample

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

   * Wrapper method for createPasswordObject
   * type_id - Password type to be used: letters, numbers or both
   * len - password length, integer between 1 and 16
   *  Response sample: {"result":{"password_object_id":243}}
      @return: String - ID password Object
   */
 
 Method declaration
 public String createPasswordObject(String type, Integer len)
