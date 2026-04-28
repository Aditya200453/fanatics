import {
  Container,
  Nav,
  Navbar,
  Button,
  Row,
  Col,
  Card,
  Dropdown,
} from "react-bootstrap";
import "./Landing.css";
import { useNavigate } from "react-router-dom";

export default function LandingPage() {
  const navigate = useNavigate();

  return (
    <div className="lp">
      {/* NAVBAR */}
      <Navbar expand="lg" className="lp-navbar" variant="dark" fixed="top">
        <Container fluid className="px-4">
          <Navbar.Brand className="fw-bold">ClinicCare</Navbar.Brand>
          <Navbar.Toggle aria-controls="lp-nav" />
          <Navbar.Collapse id="lp-nav">
            <Nav className="mx-auto gap-lg-3">
              <Nav.Link href="#home">Home</Nav.Link>
              <Nav.Link href="#about">About</Nav.Link>
              <Nav.Link href="#services">Our Services</Nav.Link>
              {/* ❌ Roles removed */}
              <Nav.Link href="#contact">Contact</Nav.Link>
            </Nav>

            {/* RIGHT SIDE ACTIONS */}
            <Nav className="ms-auto align-items-center gap-2">
              <Dropdown align="end">
                <Dropdown.Toggle variant="info" className="ms-1">
                  Login
                </Dropdown.Toggle>

                <Dropdown.Menu>
                  <Dropdown.Item onClick={() => navigate("/login/admin")}>
                    Admin Login
                  </Dropdown.Item>
                  <Dropdown.Item onClick={() => navigate("/login/doctor")}>
                    Doctor Login
                  </Dropdown.Item>
                  <Dropdown.Item onClick={() => navigate("/login/patient")}>
                    Patient Login
                  </Dropdown.Item>
                  <Dropdown.Divider />
                  <Dropdown.Item onClick={() => navigate("/login/other")}>
                    Other User Login
                  </Dropdown.Item>
                </Dropdown.Menu>
              </Dropdown>

              <Button
                variant="info"
                className="ms-1"
                onClick={() => navigate("/register")}
              >
                Register
              </Button>
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      
      {/* HERO SECTION */}
      <section id="home" className="lp-hero">
        <div className="lp-hero-overlay" />
        <Container className="lp-hero-content">
          <Row className="align-items-center">
            <Col lg={8}>
              <h1 className="lp-title">
                Clinic Management <span className="lp-accent">System</span>
              </h1>

              <p className="lp-subtitle">
                Manage appointments, patient history, consultations and
                diagnostics in one role‑based platform.
              </p>

              <div className="d-flex flex-wrap gap-2 mt-3">
                <Button
                  variant="info"
                  size="lg"
                  onClick={() => navigate("/register")}
                >
                  Get Started
                </Button>

                <Button
                  variant="outline-light"
                  size="lg"
                  onClick={() =>
                    document
                      .getElementById("services")
                      ?.scrollIntoView({ behavior: "smooth" })
                  }
                >
                  Learn More
                </Button>
              </div>
            </Col>
          </Row>
        </Container>
      </section>

{/* ABOUT SECTION */}
<section id="about" className="lp-section bg-light">
  <Container>
    <h2 className="text-center mb-5 fw-bold">
      What our <span className="lp-accent">ClinicCare does</span>?
    </h2>

    <Row className="g-4">
      {[
        {
          icon: "bi-clipboard-heart",
          title: "Complete Patient Management",
          desc: "Manage patient registrations, medical history, visit records, and treatment details in a centralized and secure system.",
        },
        {
          icon: "bi-calendar-check",
          title: "Appointment & Scheduling System",
          desc: "Handle doctor availability, patient appointments, follow-ups, and cancellations with an organized scheduling workflow.",
        },
        {
          icon: "bi-prescription2",
          title: "Prescriptions & Treatment Records",
          desc: "Digitally manage prescriptions, medicines, dosage instructions, and treatment plans for easy access and continuity of care.",
        },
        {
          icon: "bi-file-earmark-medical",
          title: "Diagnostics & Medical Reports",
          desc: "Assign diagnostic tests, store lab reports, and maintain accurate records linked directly to patient profiles.",
        },
        {
          icon: "bi-people-fill",
          title: "Doctor & Staff Coordination",
          desc: "Enable smooth coordination between doctors, patients, and administrative staff through role-based system access.",
        },
        {
          icon: "bi-shield-lock",
          title: "Secure & Role-Based Access",
          desc: "Ensure data privacy and security with controlled access for Admins, Doctors, and Patients based on their roles.",
        },
      ].map((item) => (
        <Col md={6} lg={4} key={item.title}>
          <Card className="lp-feature-card h-100 border-0 about-card">
            <Card.Body className="p-4 text-center">
              <div className="lp-feature-icon mb-3">
                <i className={`bi ${item.icon}`} />
              </div>

              <Card.Title className="fw-bold mb-2">
                {item.title}
              </Card.Title>

              <Card.Text className="text-muted mb-0">
                {item.desc}
              </Card.Text>
            </Card.Body>
          </Card>
        </Col>
      ))}
    </Row>
  </Container>
</section>
      {/* SERVICES */}
      <section id="services" className="lp-section">
        <Container>
          <h2 className="text-center mb-4 fw-bold">Key Capabilities</h2>
          <Row className="g-4">
            {[
              {
                icon: "bi-calendar2-check",
                title: "Appointment Booking",
                desc: "Book appointments by specialty and doctor.",
              },
              {
                icon: "bi-folder2-open",
                title: "Patient Records",
                desc: "Consistent patient history & visit timeline.",
              },
              {
                icon: "bi-prescription2",
                title: "Prescriptions",
                desc: "Store prescriptions and fetch anytime.",
              },
              {
                icon: "bi-file-earmark-medical",
                title: "Diagnostics",
                desc: "Assign tests and maintain results.",
              },
            ].map((x) => (
              <Col md={6} lg={3} key={x.title}>
                <Card className="lp-feature-card h-100 border-0">
                  <Card.Body className="p-4 text-center">
                    <div className="lp-feature-icon">
                      <i className={`bi ${x.icon}`} />
                    </div>
                    <Card.Title className="mt-3 fw-bold">
                      {x.title}
                    </Card.Title>
                    <Card.Text className="text-muted mb-0">
                      {x.desc}
                    </Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>

      {/* FOOTER */}
      <footer id="contact" className="lp-footer">
        <Container className="py-4 d-flex flex-column flex-md-row justify-content-between gap-2">
          <div className="text-white-50">
            © {new Date().getFullYear()} ClinicCare
          </div>
          <div className="text-white-50">Privacy • Terms • Support</div>
        </Container>
      </footer>
    </div>
  );
}