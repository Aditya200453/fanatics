import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap-icons/font/bootstrap-icons.css";
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
        <Container fluid className="px-4 lp-navwrap">
          <Navbar.Brand className="fw-bold">ClinicCare</Navbar.Brand>
 
          <Navbar.Toggle aria-controls="lp-nav" className="lp-toggler" />
 
          <Navbar.Collapse id="lp-nav" className="lp-collapse">
            {/* CENTER LINKS */}
            <Nav className="lp-nav-center">
              <Nav.Link href="#home">Home</Nav.Link>
              <Nav.Link href="#about">About</Nav.Link>
              <Nav.Link href="#services">Our Services</Nav.Link>
              <Nav.Link href="#contact">Contact</Nav.Link>
            </Nav>
 
            {/* RIGHT SIDE ACTIONS */}
            <Nav className="lp-nav-actions">
              <Button
                variant="info"
                className="lp-btn lp-login-btn"
                onClick={() => navigate("/login")}
              >
                Login
              </Button>
 
              <Button
                variant="info"
                className="lp-btn"
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
                <span className="lp-accent">ClinicCare</span>
              </h1>
 
 <p className="lp-subtitle">
  Simplify clinic operations with ClinicCare — a modern platform to manage
  patients, appointments, prescriptions, and staff efficiently in one place.
</p>
 
 
              <div className="lp-hero-actions">
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
                    <Card.Title className="mt-3 fw-bold">{x.title}</Card.Title>
                    <Card.Text className="text-muted mb-0">{x.desc}</Card.Text>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </section>
 
      {/* FOOTER */}
      {/* FOOTER */}
      <footer id="contact" className="lp-footer">
        <Container className="py-5">
          <Row className="g-4">
 
            {/* ✅ LEFT - BRAND */}
            <Col md={4}>
              <h5 className="fw-bold text-white mb-3">ClinicCare</h5>
              <p className="text-white-50 small">
                A complete clinic management system for handling patients,
                appointments, prescriptions, and diagnostics with ease.
              </p>
            </Col>
 
            {/* ✅ COMPANY LINKS */}
            <Col md={2}>
              <h6 className="text-white mb-3">Company</h6>
              <ul className="list-unstyled">
                <li><a href="#about" className="footer-link">About</a></li>
                <li><a href="#services" className="footer-link">Services</a></li>
                <li><a href="#contact" className="footer-link">Contact</a></li>
              </ul>
            </Col>
 
            {/* ✅ CONTACT DETAILS */}
            <Col md={4}>
              <h6 className="text-white mb-3">Contact Us</h6>
              <ul className="list-unstyled text-white-50 small">
 
                <li className="mb-2">
                  <i className="bi bi-geo-alt me-2"></i>
                  Hyderabad, Telangana, India
                </li>
 
                <li className="mb-2">
                  <i className="bi bi-telephone me-2"></i>
                  +91 98765 43210
                </li>
 
                <li className="mb-2">
                  <i className="bi bi-envelope me-2"></i>
                  support@cliniccare.com
                </li>
 
                <li className="mb-2">
                  <i className="bi bi-clock me-2"></i>
                  Mon - Sat: 9:00 AM - 6:00 PM
                </li>
 
              </ul>
            </Col>
 
          </Row>
 
          {/* ✅ BOTTOM */}
          <div className="mt-4 pt-3 border-top d-flex flex-column flex-md-row justify-content-between text-white-50 small">
            <div>© {new Date().getFullYear()} ClinicCare. All rights reserved.</div>
            <div>Privacy • Terms • Support</div>
          </div>
        </Container>
      </footer>
 
    </div>
  );
}
 