import axios from "axios";

const API_URL = "http://localhost:8080/api/auth/";

class AuthService {
  signIn(username, password) {
    return axios
      .post(API_URL + "sign-in", {
        username,
        password,
      })
      .then((response) => {
        if (response.data.accessToken !== null) {
          //localStorage.setItem("user", JSON.stringify(response.data));
          localStorage.setItem(
            "username",
            JSON.stringify(response.data.username)
          );
          localStorage.setItem("jwt", response.data.accessToken);
        }
        return response.data;
      });
  }

  signOut() {
    localStorage.removeItem("jwt");
    localStorage.removeItem("username");
  }

  signUp(username, fullName, password, age) {
    return axios
      .post(API_URL + "sign-up", {
        username,
        fullName,
        password,
        age,
      })
      .then((response) => {
        if (response.status !== 201) return response.message;
        return response;
      });
  }

  getCurrentUser() {
    return JSON.parse(localStorage.getItem("user"));
  }
}

// eslint-disable-next-line import/no-anonymous-default-export
export default new AuthService();
