import BaseLogin from "./BaseLogin";
 
export default function OthersLogin({ title, expectedRole }) {
  return (
    <BaseLogin
      title={title}
      expectedRole={expectedRole}
    />
  );
}
 