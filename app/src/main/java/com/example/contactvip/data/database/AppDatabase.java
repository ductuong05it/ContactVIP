package com.example.contactvip.data.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.contactvip.data.dao.CallHistoryDao;
import com.example.contactvip.data.dao.ContactDao;
import com.example.contactvip.data.entity.CallHistory;
import com.example.contactvip.data.entity.Contact;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Contact.class, CallHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ContactDao contactDao();
    public abstract CallHistoryDao callHistoryDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "phone_contacts_db")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                ContactDao dao = INSTANCE.contactDao();
                
                Contact contact1 = new Contact();
                contact1.firstName = "Nguyễn Văn";
                contact1.lastName = "An";
                contact1.phoneNumber = "0987654321";
                contact1.isFavorite = true;
                dao.insert(contact1);

                Contact contact2 = new Contact();
                contact2.firstName = "Trần Minh";
                contact2.lastName = "Đức";
                contact2.phoneNumber = "0912345678";
                contact2.isFavorite = true;
                dao.insert(contact2);

                Contact contact3 = new Contact();
                contact3.firstName = "Lê Hoàng";
                contact3.lastName = "Nam";
                contact3.phoneNumber = "0901234567";
                dao.insert(contact3);
            });
        }
    };
}
