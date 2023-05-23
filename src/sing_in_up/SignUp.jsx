import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AuthService from "../services/auth-service";

function SignUp() {
  const [username, setUsername] = useState("");
  const [fullName, setFullName] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [age, setAge] = useState(0);

  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();

  const submitUser = (e) => {
    e.preventDefault();

    AuthService.signUp(username, fullName, password, age)
      .then((response) => {
        console.log(response);
        navigate("/signin");
        window.location.reload();
      })
      .catch((error) => {
        setErrorMessage(
          error.message +
            "The username already exists, try with another username!"
        );
      });
  };

  return (
    <div className="row">
      <form className="col s12">
        <h1>Sign up</h1>
        {errorMessage && <div>{errorMessage}</div>}
        <div className="row">
          <div className="input-field col s6">
            <input
              id="username"
              placeholder="Username"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              type="text"
              className="validate"
            />
            <label htmlFor="username">Username</label>
          </div>
          <div className="input-field col s6">
            <input
              id="fullname"
              placeholder="Full name"
              value={fullName}
              onChange={(event) => setFullName(event.target.value)}
              type="text"
              className="validate"
            />
            <label htmlFor="fullname">Full Name</label>
          </div>
          <div className="input-field col s6">
            <input
              id="password"
              placeholder="Password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              type="password"
              className="validate"
            />
            <label htmlFor="password">Password</label>
          </div>
          <div className="input-field col s6">
            <input
              id="confirmPassword"
              placeholder="Confirm password"
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
              type="password"
              className="validate"
            />
            <label htmlFor="confirmPassword">Confirm Password</label>
          </div>
          <div className="input-field col s6">
            <input
              id="age"
              placeholder="Age"
              value={age}
              onChange={(event) => setAge(event.target.value)}
              type="text"
              className="validate"
            />
            <label htmlFor="age">Age</label>
          </div>
          <hr />
          <div>
            <button
              type="subnmit"
              name="action"
              disabled={
                !username ||
                !password ||
                !fullName ||
                !age ||
                password !== confirmPassword
              }
              onClick={submitUser}
              className="btn waves-effect waves-light deep-purple darken-4"
            >
              Sign Up
            </button>
            <button
              onClick={() => {
                navigate("/signin");
                window.location.reload();
              }}
              className="btn waves-effect waves-light deep-purple darken-4"
            >
              Sign In
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}

export default SignUp;
