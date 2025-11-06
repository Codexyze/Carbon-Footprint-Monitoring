import { redirect } from 'next/navigation'

export default function Page() {
  // Redirect /users to the tenants users page which contains the user form
  // This is a small, reversible change to restore the expected URL behavior.
  redirect('/tenants/users')
}
