import AuthService from "../services/auth-service";

function SignOut() {
  const onConfirmClicked = async () => {
    AuthService.signOut();
    window.location.reload();
  };

  return (
    <div className="content-container">
      <h1>Sure you want to log out?</h1>
      <hr />
      <button
        onClick={onConfirmClicked}
        className="btn deep-purple darken-4 waves-effect waves-light"
      >
        Yes!
      </button>
    </div>
  );
}

export default SignOut;
