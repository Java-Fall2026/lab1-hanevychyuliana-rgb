package ua.hotel;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.hotel.loader.DataLoader;
import ua.hotel.loader.FileStorage;
import ua.hotel.model.Guest;
import ua.hotel.model.Room;
import ua.hotel.repository.GuestRepository;
import ua.hotel.repository.RoomRepository;
import ua.hotel.util.AppConfig;
import ua.hotel.util.ObjectMapperFactory;
import ua.hotel.web.ApiServer;

import java.nio.file.Path;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // 1. Read configuration from app.properties
        AppConfig config = AppConfig.loadFromClasspath("app.properties");
        int configuredPort = config.getPort();
        Path roomsInput = config.getPath("data.rooms.input");
        Path guestsInput = config.getPath("data.guests.input");
        Path roomsOutput = config.getPath("data.rooms.output");
        Path guestsOutput = config.getPath("data.guests.output");

        ObjectMapper mapper = ObjectMapperFactory.create();
        RoomRepository roomRepo = new RoomRepository();
        GuestRepository guestRepo = new GuestRepository();

        // 2. Load initial datasets using DataLoader and print summary
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            DataLoader loader = new DataLoader(mapper, validator);

            DataLoader.LoadReport roomsReport = loader.load(roomsInput, Room.class, roomRepo);
            System.out.printf("Кімнати (%s): прочитано=%d, додано=%d, відкинуто=%d%n",
                    roomsInput, roomsReport.read(), roomsReport.added(), roomsReport.rejected());

            DataLoader.LoadReport guestsReport = loader.load(guestsInput, Guest.class, guestRepo);
            System.out.printf("Гості (%s): прочитано=%d, додано=%d, відкинуто=%d%n",
                    guestsInput, guestsReport.read(), guestsReport.added(), guestsReport.rejected());
        }

        // 3. Start embedded Jetty server
        ApiServer apiServer = new ApiServer(configuredPort, roomRepo, guestRepo, mapper);
        apiServer.start();
        int actualPort = apiServer.port();
        System.out.println("Сервер запущено за адресою: http://localhost:" + actualPort);
        System.out.println("Ендпоінти: http://localhost:" + actualPort + "/api/rooms та http://localhost:" + actualPort + "/api/guests");

        // 4. Register shutdown hook to stop server and persist repositories
        FileStorage fileStorage = new FileStorage(mapper);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nЗупинка сервера та збереження даних...");
            try {
                apiServer.stop();
                fileStorage.writeList(roomsOutput, roomRepo.findAll());
                fileStorage.writeList(guestsOutput, guestRepo.findAll());
                System.out.println("Дані успішно збережено у " + roomsOutput + " та " + guestsOutput);
            } catch (Exception e) {
                log.error("Error during graceful shutdown", e);
            }
        }, "shutdown-hook"));
    }
}