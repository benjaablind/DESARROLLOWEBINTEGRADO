export interface NavItem {
  label: string;
  path: string;
  icon: string;
}

export const NAV_ITEMS: NavItem[] = [
  { label: 'Dashboard', path: '/dashboard', icon: '📊' },
  { label: 'Pacientes', path: '/pacientes', icon: '🧑‍🤝‍🧑' },
  { label: 'Citas', path: '/citas', icon: '📅' },
  { label: 'Historia Clínica', path: '/historia-clinica', icon: '📋' },
  { label: 'Odontograma', path: '/odontograma', icon: '🦷' },
  { label: 'Tratamientos', path: '/tratamientos', icon: '💉' },
  { label: 'Pagos', path: '/pagos', icon: '💳' },
  { label: 'Archivos', path: '/archivos', icon: '🗂️' },
  { label: 'Reportes', path: '/reportes', icon: '📈' },
  { label: 'Usuarios', path: '/usuarios', icon: '👤' },
];
