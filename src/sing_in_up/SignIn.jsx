import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/auth-service";

function SignIn() {
  const [errorMessage, setErrorMessage] = useState("");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const navigate = useNavigate();

  const onLogInClicked = (e) => {
    e.preventDefault();

    AuthService.signIn(username, password)
      .then((res) => {
        if (errorMessage !== null) {
          navigate("/homepage");
          window.location.reload();
        } else {
          setErrorMessage("Something went bananas, try again!");
        }
      })
      .catch((err) => {
        setErrorMessage(err.message);
      });
  };

  return (
    <div className="row">
      <form className="col s12">
        <h1>Log In</h1>
        {errorMessage && <div>{errorMessage}</div>}
        <div className="row">
          <div className="input-field col s6">
            <input
              id="emailaddress"
              className="validate"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="your-email@something.com"
            />
            <label htmlFor="emailaddress">Email Address</label>
          </div>
          <div className="input-field col s6">
            <input
              id="password"
              className="validate"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="password"
            />
            <label htmlFor="password">Password</label>
          </div>
          <hr />
          <div className="row center-align">
            <button
              className="btn waves-effect waves-light deep-purple darken-4"
              disabled={username && password ? false : true}
              onClick={onLogInClicked}
              type="submit"
              name="action"
            >
              Log In
            </button>
          </div>
          <div className="row center-align">
            <button
              onClick={() => {
                navigate("/signup");
                window.location.reload();
              }}
              className="btn waves-effect waves-light deep-purple darken-4"
            >
              Don't have an acount? Sign Up
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}

export default SignIn;
