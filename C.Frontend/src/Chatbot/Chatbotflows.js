// src/components/chatbot/chatbotFlows.js

export const BOT_NAME = "Maddy"

export const GREETING =
  "Hi! 👋 I’m Maddy, your clinic assistant. How can I help you today?";

export const FLOWS = {
  root: {
   message: GREETING,
    options: [
      { id: "hours", label: "When does the clinic open?" },
      { id: "book", label: "How to book an appointment?" },
      { id: "specialities", label: "What doctor specialities are available?" },
      { id: "services", label: "What services are available?" },
      { id: "agent", label: "Talk to an agent" },
    ],
  },

  hours: {
    answer:
      "Clinic hours: Mon–Sat 9:00 AM–6:00 PM. Sunday: Closed. (If your clinic timings differ, update this text in chatbotFlows.js)",
    next: [
      { id: "book", label: "How to book an appointment?" },
      { id: "services", label: "What services are available?" },
      { id: "root", label: "Back to main menu" },
    ],
  },

  book: {
    answer:
      "To book an appointment: Go to Patient Dashboard → Book Appointment → Select Speciality → Select Doctor → Choose Date & Time → Confirm.",
    next: [
      { id: "specialities", label: "See doctor specialities" },
      { id: "hours", label: "Clinic timings" },
      { id: "root", label: "Back to main menu" },
    ],
  },

  specialities: {
    answer:
      "Available specialities: Cardiology, Dermatology, Pediatrics, Orthopedics, ENT, General Physician. (Edit this list as per your system.)",
    next: [
      { id: "book", label: "Book an appointment" },
      { id: "services", label: "Other services" },
      { id: "root", label: "Back to main menu" },
    ],
  },

  services: {
    answer:
      "Services: General Consultation, Lab Tests, Diagnostics, Follow-up Visits, Prescriptions, Health Checkups. (Edit as per your clinic.)",
    next: [
      { id: "book", label: "Book an appointment" },
      { id: "hours", label: "Clinic timings" },
      { id: "root", label: "Back to main menu" },
    ],
  },

  agent: {
    answer:
      "Sure — I can connect you to a support agent. Please tap the button below to reach our team.",
    next: [{ id: "root", label: "Back to main menu" }],
    handoff: true,
  },
};
