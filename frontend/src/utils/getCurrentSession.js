export const getCurrentSession = async() => {
  const response = await fetch('http://localhost:3000/currentsession', {
    credentials: "include",
  });
  const jsonResponse = await response.json();
  return jsonResponse;
}
