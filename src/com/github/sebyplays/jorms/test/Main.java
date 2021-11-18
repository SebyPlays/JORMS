package com.github.sebyplays.jorms.test;

import com.github.sebyplays.jorms.Database;
import com.github.sebyplays.jorms.api.Row;
import com.github.sebyplays.jorms.api.TableBase;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IllegalAccessException {
        //Database database = new Database(new File(System.getProperty("user.dir") + "/data/sql/ORMTet.db"), Logger.getLogger("SQL"));

        Database database = new Database("jorm", "1!9(CgpKAsLbemnd", "jorm", "localhost", 3306, null);
        database.connect();
        database.registerTable(new Human());

        TableBase human = database.getTable(Human.class);

        human.insert(new Human("James Charles", 19), true, false);
        human.insert(new Human("15% off of your next purchase", 113), true, false);
        human.insert(new Human("Ranjid Tech-support", 1139), true, false);
        human.insert(new Human("Hakti Shakti", 81), true, false);
        human.insert(new Human("Sasha Hamilton", 11321), true, false);

        if(human.getRow().where("name").equals("John Doe").exists())
            human.getRow().where("name").equals("John Doe").set("age", 471);

        for(Row row : human.getRows())
            System.out.println(row.get("name") + " " + row.get("age"));


        database.disconnect();
    }

}
