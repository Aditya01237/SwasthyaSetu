import { initializeApp } from "firebase/app";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyAY-JCFI09Oonihsrxe-5EVvdDOFiveJsI",
  authDomain: "swasthyasetu-521c4.firebaseapp.com",
  projectId: "swasthyasetu-521c4",
  storageBucket: "swasthyasetu-521c4.firebasestorage.app",
  messagingSenderId: "983875911122",
  appId: "1:983875911122:web:7d338e93e5a063deb01c0b"
};

const app = initializeApp(firebaseConfig);

export const auth = getAuth(app);