package support;
import android.icu.text.SimpleDateFormat;
import android.util.Log;

import java.util.Date;

/**
 * Created by Kirill on 16.04.2017.
 */

public class ParseData {


    public String ParseData(String data){
        String Str = "";
        Date GMTTime=new Date(Long.parseLong(data + "000"));
        SimpleDateFormat a = new SimpleDateFormat("kk:mm");
        SimpleDateFormat b = new SimpleDateFormat("dd MMM");
        SimpleDateFormat c = new SimpleDateFormat("dd, MMMM yy");
        Date datNow = new Date();//получаем текущюю дату и время

        SimpleDateFormat one = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat two = new SimpleDateFormat("yyyy");
        if(one.format(datNow).equals(one.format(GMTTime))){
            Str = a.format(GMTTime);}
        else {if(two.format(datNow).equals(two.format(GMTTime))) {
            Str = b.format(GMTTime);
        }
        else Str = c.format(GMTTime);}
        return Str;
    }
}
