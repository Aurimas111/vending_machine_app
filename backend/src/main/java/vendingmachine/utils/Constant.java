package vendingmachine.utils;

//import io.github.cdimascio.dotenv.Dotenv;

public class Constant {
        /*static Dotenv dotenv = Dotenv.configure().directory("src/main/.env").load(); // load .env file
        public static final String BF_PROJECT_KEY = dotenv.get("BLOCKFROST_API_KEY");
        public static final String IPFS_KEY = dotenv.get("IPFS_API_KEY");
        public static final String RECOVERY_PHRASE = dotenv.get("RECOVERY_PHRASE");*/

        public static final String BF_PROJECT_KEY = System.getenv("BLOCKFROST_API_KEY");
        public static final String IPFS_KEY = System.getenv("IPFS_API_KEY");
        public static final String RECOVERY_PHRASE = System.getenv("RECOVERY_PHRASE");
        public static final String DB_URL = System.getenv("DB_URL");
        public static final String DB_USER = System.getenv("DB_USER");
        public static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
}